package jaicore.basic.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.concurrent.InterruptionTimerTask;

public abstract class AAlgorithm<I, O> implements IAlgorithm<I, O>, ILoggingCustomizable {

	/* Logger variables */
	private Logger logger = LoggerFactory.getLogger(AAlgorithm.class);
	private String loggerName;

	/* Parameters of the algorithm. */
	private IAlgorithmConfig config;

	/* Semantic input to the algorithm. */
	private final I input;

	/* State and event bus for sending algorithm events. */
	private Timer timer;
	private long shutdownInitialized = -1; // timestamp for when the shutdown has been initialized
	private long activationTime = -1; // timestamp of algorithm activation
	
	private String id;
	private long deadline = -1; // timestamp when algorithm must terminate due to timeout
	private long timeOfTimeoutDetection = -1; // timestamp for when timeout has been triggered
	private long canceled = -1; // timestamp for when the algorithm has been canceled
	private final Set<Thread> activeThreads = new HashSet<>();
	private AlgorithmState state = AlgorithmState.created;
	private final EventBus eventBus = new EventBus();
	
	private int timeoutPrecautionOffset = 2000; // this offset is substracted from the true remaining time whenever a timer is scheduled to ensure that the timeout is respected
	private static int MIN_RUNTIME_FOR_OBSERVED_TASK = 50;
	private final Collection<Thread> threadsInterruptedByShutdown = new ArrayList<>(); 

	/**
	 * C'tor providing the input for the algorithm already.
	 *
	 * @param input
	 *            The input for the algorithm.
	 */
	protected AAlgorithm(final I input) {
		this.input = input;
		this.config = ConfigFactory.create(IAlgorithmConfig.class);
	}

	/**
	 * Internal c'tore overwriting the internal configuration and setting the input.
	 *
	 * @param input
	 *            The input for the algorithm.
	 * @param config
	 *            The configuration to take as the internal configuration object.
	 */
	protected AAlgorithm(final IAlgorithmConfig config, final I input) {
		this.config = config;
		this.input = input;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return this.state != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		try {
			return this.nextWithException();
		} catch (Exception e) {
			this.unregisterThreadAndShutdown();
			throw new RuntimeException(e);
		}
	}

	@Override
	public I getInput() {
		return this.input;
	}

	@Override
	public void registerListener(final Object listener) {
		this.eventBus.register(listener);
	}

	@Override
	public int getNumCPUs() {
		return this.getConfig().cpus();
	}

	@Override
	public void setNumCPUs(final int numberOfCPUs) {
		this.getConfig().setProperty(IAlgorithmConfig.K_CPUS, numberOfCPUs + "");
	}
	
	@Override
	public void setMaxNumThreads(int maxNumberOfThreads) {
		this.getConfig().setProperty(IAlgorithmConfig.K_THREADS, maxNumberOfThreads + "");
	}

	@Override
	public void setTimeout(final long timeout, final TimeUnit timeUnit) {
		this.setTimeout(new TimeOut(timeout, timeUnit));
	}

	@Override
	public void setTimeout(final TimeOut timeout) {
		this.logger.info("Setting timeout to {}ms", timeout.milliseconds());
		this.getConfig().setProperty(IAlgorithmConfig.K_TIMEOUT, timeout.milliseconds() + "");
	}

	@Override
	public TimeOut getTimeout() {
		return new TimeOut(this.getConfig().timeout(), TimeUnit.MILLISECONDS);
	}

	public boolean isTimeouted() {
		if (this.timeOfTimeoutDetection > 0)
			return true;
		if (this.deadline > 0 && System.currentTimeMillis() >= this.deadline) {
			this.timeOfTimeoutDetection = System.currentTimeMillis();
			return true;
		}
		return false;
	}

	protected TimeOut getRemainingTimeToDeadline() {
		if (this.deadline < 0) {
			return new TimeOut(Integer.MAX_VALUE, TimeUnit.SECONDS);
		}
		return new TimeOut(this.deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public boolean isStopCriterionSatisfied() {
		return this.isCanceled() || this.isTimeouted() || Thread.currentThread().isInterrupted();
	}

	protected Timer getTimerAndCreateIfNotExistent() {
		if (this.timer == null) {
			this.timer = new Timer("Timer for algorithm " + getId());
		}
		return this.timer;
	}

	/**
	 * @return Flag denoting whether this algorithm has been canceled.
	 */
	public boolean isCanceled() {
		return this.canceled > 0;
	}

	@Override
	public String getId() {
		if (id == null) {
			id = getClass().getName() + "-" + System.currentTimeMillis();
		}
		return id;
	}

	protected void checkAndConductTermination() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		this.logger.debug("Checking Termination");
		if (isTimeouted()) {
			this.logger.info("Timeout detected for {}, shutting down the algorithm and stopping execution with TimeoutException", this.getId());
			logger.debug("Invoking shutdown");
			this.unregisterThreadAndShutdown();
			logger.debug("Throwing TimeoutException");
			throw new AlgorithmTimeoutedException(this.timeOfTimeoutDetection - this.deadline);
		}
		if (this.isCanceled()) {
			this.logger.info("Cancel detected for {}, stopping execution with AlgorithmExceptionCanceledException", this.getId());
			this.unregisterThreadAndShutdown(); // calling cancel() usually already shutdowns, but this behavior may have been overwritten
			if (hasThreadBeenInterruptedDuringShutdown(Thread.currentThread())) {
				Thread t = Thread.currentThread();
				logger.debug("Reset interrupt flag of thread {} since thread has been interrupted during shutdown but not from the outside. Current interrupt flag is {}", t, t.isInterrupted());
				Thread.interrupted(); // reset interrupted flag
			}
			throw new AlgorithmExecutionCanceledException(System.currentTimeMillis() - this.canceled);
		}
		if (Thread.currentThread().isInterrupted()) {
			this.logger.info("Interruption detected for {}, stopping execution with InterruptedException. Resetting interrupted-flag.", this.getId());
			Thread.interrupted(); // clear the interrupt-field. This is necessary, because otherwise some shutdown-activities (like waiting for pool shutdown) might fail
			this.unregisterThreadAndShutdown();
			Thread.currentThread().interrupt(); // interrupt again to double-inform the invoker (not only via Exception but also over the interrupted-flag)
			throw new InterruptedException(); // if the thread itself was actively interrupted by somebody
		}
		logger.debug("No termination condition observed.");
	}

	/**
	 * This method does two things:
	 * 1. it interrupts all threads that are registered to be active inside this algorithm
	 * 2. it cancels the (possibly created) timeout thread
	 *
	 * This method should be called ALWAYS when the algorithm activity ceases.
	 *
	 * This method takes effect only once. Further invocations will be ignored.
	 */
	protected void shutdown() {
		synchronized (this) {
			if (this.shutdownInitialized > 0) {
				this.logger.info("Tried to enter shudtown for {}, but the shutdown has already been initialized in the past, so exiting the shutdown block.", this);
				return;
			}
			this.shutdownInitialized = System.currentTimeMillis();
		}
		this.logger.info("Entering shutdown procedure for {}. Setting algorithm state from {} to inactive and interrupting {} active threads.", this.getId(), this.getState(), this.activeThreads.size());
		this.activeThreads.forEach(t -> {
			this.logger.info("Interrupting {} on behalf of shutdown of {}", t, this.getId());
			t.interrupt();
			threadsInterruptedByShutdown.add(t);
		});
		if (this.timer != null) {
			this.logger.info("Canceling timer {}", this.timer);
			this.timer.cancel();
		}
		this.logger.info("Shutdown of {} completed.", this.getId());
	}
	
	public boolean hasThreadBeenInterruptedDuringShutdown(Thread t) {
		return threadsInterruptedByShutdown.contains(t);
	}

	public boolean isShutdownInitialized() {
		return this.shutdownInitialized > 0;
	}

	protected void unregisterThreadAndShutdown() {
		this.unregisterActiveThread();
		this.shutdown();
	}

	protected void registerActiveThread() {
		this.activeThreads.add(Thread.currentThread());
	}

	protected void unregisterActiveThread() {
		logger.trace("Unregistering current thread {}", Thread.currentThread());
		this.activeThreads.remove(Thread.currentThread());
	}

	public long getActivationTime() {
		return activationTime;
	}

	/**
	 * @return The current state of the algorithm.
	 */
	public AlgorithmState getState() {
		return this.state;
	}

	/**
	 * @param state
	 *            The new state of the algorithm.
	 */
	protected void setState(final AlgorithmState state) {
		if (state == AlgorithmState.active) {
			throw new IllegalArgumentException("Cannot switch state to active. Use \"activate\" instead, which will set the state to active and provide the AlgorithmInitializedEvent.");
		} else if (state == AlgorithmState.inactive) {
			throw new IllegalArgumentException("Cannot switch state to inactive. Use \"terminate\" instead, which will set the state to inactive and provide the AlgorithmFinishedEvent.");
		}
		this.state = state;
	}

	@Override
	public void cancel() {
		if (this.isCanceled()) {
			this.logger.debug("Ignoring cancel command since the algorithm has been canceled before.");
			return;
		}
		this.canceled = System.currentTimeMillis();
		if (isShutdownInitialized()) {
			this.logger.debug("Ignoring cancel command since the algorithm has already been shutdown before.");
			return;
		}
		this.logger.info("Executing cancel on {}. Have set the cancel flag and will now invoke shutdown procedure.", this.getId());
		this.shutdown();
	}

	/**
	 * This method
	 * - defines the definite deadline for when the algorithm must have finished
	 * - sets the algorithm state to ACTIVE
	 * - sends the mandatory AlgorithmInitializedEvent over the event bus.
	 * Should only be called once and as before the state is set to something else.
	 */
	protected AlgorithmInitializedEvent activate() {
		assert this.state == AlgorithmState.created : "Can only activate an algorithm as long as its state has not been changed from CREATED to something else. It is currently " + this.state;
		this.activationTime = System.currentTimeMillis();
		if (this.getTimeout().milliseconds() > 0) {
			this.deadline = this.activationTime + this.getTimeout().milliseconds();
		}
		this.state = AlgorithmState.active;
		AlgorithmInitializedEvent event = new AlgorithmInitializedEvent(getId());
		this.eventBus.post(event);
		this.logger.info("Starting algorithm {} with problem {} and config {}", this.getId(), this.input, this.config);
		return event;
	}

	/**
	 * This methods terminates the algorithm, setting the internal state to inactive and emitting the mandatory AlgorithmFinishedEvent over the event bus.
	 *
	 * @return The algorithm finished event.
	 */
	protected AlgorithmFinishedEvent terminate() {
		logger.info("Terminating algorithm {}.", getId());
		this.state = AlgorithmState.inactive;
		AlgorithmFinishedEvent finishedEvent = new AlgorithmFinishedEvent(getId());
		this.unregisterThreadAndShutdown();
		this.eventBus.post(finishedEvent);
		return finishedEvent;
	}

	/**
	 * This methods allows for posting an event on the algorithm's event bus.
	 *
	 * @param e
	 *            The event to post on the event bus.
	 */
	protected void post(final Object e) {
		this.eventBus.post(e);
	}

	@Override
	public IAlgorithmConfig getConfig() {
		return this.config;
	}

	/**
	 * Sets the config object to the new config object.
	 *
	 * @param config
	 *            The new config object.
	 */
	public void setConfig(final IAlgorithmConfig config) {
		this.config = config;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger to {}", name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched to logger {}", name);
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	protected <T> T computeTimeoutAware(Callable<T> r) throws InterruptedException, AlgorithmException, AlgorithmExecutionCanceledException, TimeoutException {
		
		/* if no timeout is sharp, just execute the task */
		if (getTimeout().milliseconds() < 0) {
			try {
				return r.call();
			} catch (InterruptedException e) { // the fact that we are interrupted here can have several reasons. Could be an interrupt from the outside, a cancel, or a timeout by the above timer
				boolean interruptedDueToShutdown = threadsInterruptedByShutdown.contains(Thread.currentThread());
				logger.info("Received intterrupt. Cancel flag is {}. Thread contained in interrupted by shutdown: {}", isCanceled(), interruptedDueToShutdown);
				if (!interruptedDueToShutdown)
					throw e;
				checkAndConductTermination();
				throw new IllegalStateException("termination routine should have thrown an exception!");
			} catch (Exception e) {
				throw new AlgorithmException(e, "The algorithm has failed due to an exception of a Callable.");
			}
		}
		
		/* if the remaining time is not sufficient to conduct further calculation, cancel at this point */
		long remainingTime = getRemainingTimeToDeadline().milliseconds();
		if (remainingTime < timeoutPrecautionOffset + MIN_RUNTIME_FOR_OBSERVED_TASK) {
			logger.debug("Only {}ms left, which is not enough to reliably compute more successors. Terminating search at this point", remainingTime);
			Thread.sleep(remainingTime);
			checkAndConductTermination();
			throw new IllegalStateException("termination routine should have thrown an exception!");
		}
		
		/* schedule a timer that will interrupt the current thread and execute the task */
		long timeToInterrupt = remainingTime - timeoutPrecautionOffset;
		Timer t = getTimerAndCreateIfNotExistent();
		AtomicBoolean timeoutTriggered = new AtomicBoolean(false);
		TimerTask task = new InterruptionTimerTask("Timeout triggered", () -> {
			logger.debug("Timeout detected at timestamp {}. This is  {} prior to deadline, interrupting successor generation.", System.currentTimeMillis(), getRemainingTimeToDeadline());
			timeoutTriggered.set(true);
		});
		logger.debug("Scheduling timer for interruption in {}ms, i.e. timestamp {}. Remaining time to deadline: {}", timeToInterrupt, System.currentTimeMillis() + timeToInterrupt, getRemainingTimeToDeadline());
		t.schedule(task, timeToInterrupt);
		try {
			logger.debug("Starting supervised computation of {}.", r);
			T result = r.call();
			task.cancel();
			return result;
		} catch (InterruptedException e) { // the fact that we are interrupted here can have several reasons. Could be an interrupt from the outside, a cancel, or a timeout by the above timer
			logger.info("Received intterrupt. Cancel flag is {}", isCanceled());
			
			/* if the timeout has been triggered (with caution), just sleep until */
			remainingTime = getRemainingTimeToDeadline().milliseconds();
			if (timeoutTriggered.get()) {
				Thread.interrupted(); // clear the interrupted field
				if (remainingTime > 0) {
					logger.debug("Artificially sleeping {}ms to trigger the correct behavior in the checker.", remainingTime);
					Thread.sleep(remainingTime);
				} else {
					logger.debug("Gained back control from successor generation, but remaining time is now only {}ms. Algorithm should terminate now.", remainingTime);
				}
			}
			
			/* otherwise, if the thread has been interrupted directly and not as a consequence of a shutdown, forward the interrupt */
			else {
				boolean interruptedDueToShutdown = threadsInterruptedByShutdown.contains(Thread.currentThread());
				logger.info("Received intterrupt. Cancel flag is {}. Thread contained in interrupted by shutdown: {}", isCanceled(), interruptedDueToShutdown);
				if (!interruptedDueToShutdown)
					throw e;
			}
			checkAndConductTermination();
			throw new IllegalStateException("termination routine should have thrown an exception!");
		} catch (Exception e) {
			throw new AlgorithmException(e, "The algorithm has failed due to an exception of a Callable.");
		} finally {
			task.cancel();
		}
	}
}
