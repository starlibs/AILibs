package jaicore.basic.algorithm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.exceptions.DelayedCancellationCheckException;
import jaicore.basic.algorithm.exceptions.DelayedTimeoutCheckException;

public abstract class AAlgorithm<I, O> implements IAlgorithm<I, O>, ILoggingCustomizable {

	/* Logger variables */
	private Logger logger = LoggerFactory.getLogger(AAlgorithm.class);
	private String loggerName;

	/* Parameters of the algorithm. */
	private IAlgorithmConfig config;

	/* Semantic input to the algorithm. */
	private I input;

	/* State and event bus for sending algorithm events. */
	private Timer timer;
	private long shutdownInitialized = -1; // timestamp for when the shutdown has been initialized
	private long activationTime = -1; // timestampe of algorithm activation
	private long deadline = -1; // timestamp when algorithm must terminate due to timeout
	private long timeOfTimeoutDetection = -1; // timestamp for when timeout has been triggered
	private long canceled = -1; // timestamp for when the algorithm has been canceled
	private final Set<Thread> activeThreads = new HashSet<>();
	private AlgorithmState state = AlgorithmState.created;
	private final EventBus eventBus = new EventBus();

	/**
	 * Standard c'tor without any parameters.
	 */
	protected AAlgorithm() {
		super();
		this.config = ConfigFactory.create(IAlgorithmConfig.class);
	}

	/**
	 * C'tor providing the input for the algorithm already.
	 *
	 * @param input
	 *            The input for the algorithm.
	 */
	protected AAlgorithm(final I input) {
		this();
		this.input = input;
	}

	/**
	 * Internal c'tor overwriting the internal config to keep the config consistent.
	 *
	 * @param config
	 *            The config to take as the internal config object.
	 */
	protected AAlgorithm(final IAlgorithmConfig config) {
		super();
		this.config = config;
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
		this(config);
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

	public void setInput(final I input) {
		this.input = input;
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
		return new TimeOut(this.deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public boolean isStopCriterionSatisfied() {
		return this.isCanceled() || this.isTimeouted() || Thread.currentThread().isInterrupted();
	}

	protected Timer getTimerAndCreateIfNotExistent() {
		if (this.timer == null) {
			this.timer = new Timer("Algorithm Timer for " + this, true);
		}
		return this.timer;
	}

	/**
	 * @return Flag denoting whether this algorithm has been canceled.
	 */
	public boolean isCanceled() {
		return this.canceled > 0;
	}
	
	protected void checkAndConductTermination() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, DelayedTimeoutCheckException, DelayedCancellationCheckException {
		this.logger.debug("Checking Termination of {}", this);
		assert !isShutdownInitialized() : "checkTermination should not be called after the shutdown has been initialized!";
		if (isTimeouted()) {
			this.logger.info("Timeout detected for {}, shutting down the algorithm and stopping execution with TimeoutException", this);
			logger.debug("Invoking shutdown");
			this.unregisterThreadAndShutdown();
			logger.debug("Throwing TimeoutException");
			TimeoutException e = new TimeoutException();
			if (this.timeOfTimeoutDetection - this.deadline > 500) {
				throw new DelayedTimeoutCheckException(e, this.timeOfTimeoutDetection - this.deadline);
			} else {
				throw e;
			}
		}
		if (this.isCanceled()) {
			this.unregisterThreadAndShutdown(); // calling cancel() usually already shutdowns, but this behavior may have been overwritten
			this.logger.info("Cancel detected for {}, stopping execution with AlgorithmExceptionCanceledException", this);
			AlgorithmExecutionCanceledException e = new AlgorithmExecutionCanceledException(); // for a controlled cancel from outside on the algorithm
			if (System.currentTimeMillis() - this.canceled > 100) {
				throw new DelayedCancellationCheckException(e, System.currentTimeMillis() - this.canceled);
			} else {
				throw e;
			}
		}
		if (Thread.currentThread().isInterrupted()) {
			this.unregisterThreadAndShutdown();
			this.logger.info("Interruption detected for {}, stopping execution with InterruptedException", this);
			throw new InterruptedException(); // if the thread itself was actively interrupted by somebody
		}
		logger.debug("No termination condition observed.");
	}

	/**
	 * This method does two things:
	 *   1. it interrupts all threads that are registered to be active inside this algorithm
	 *   2. it cancels the (possibly created) timeout thread
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
		this.logger.info("Entering shutdown procedure for {}. Setting algorithm state from {} to inactive and interrupting {} active threads.", this, this.getState(), this.activeThreads.size());
		this.activeThreads.forEach(t -> {
			this.logger.info("Interrupting {} on behalf of shutdown of {}", t, this);
			t.interrupt();
		});
		if (this.timer != null) {
			this.logger.info("Canceling timer {}", this.timer);
			this.timer.cancel();
		}
		this.logger.info("Shutdown of {} completed.", this);
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
		this.logger.info("Executing cancel on {}. Have set the cancel flag and will now invoke shutdown procedure.", this);
		this.shutdown();
	}

	/**
	 * This method
	 *  - defines the definite deadline for when the algorithm must have finished
	 *  - sets the algorithm state to ACTIVE
	 *  - sends the mandatory AlgorithmInitializedEvent over the event bus.
	 * Should only be called once and as before the state is set to something else.
	 */
	protected AlgorithmInitializedEvent activate() {
		assert this.state == AlgorithmState.created : "Can only activate an algorithm as long as its state has not been changed from CREATED to something else. It is currently " + this.state;
		this.activationTime = System.currentTimeMillis();
		if (this.getTimeout().milliseconds() > 0) {
			this.deadline = this.activationTime + this.getTimeout().milliseconds();
		}
		this.state = AlgorithmState.active;
		AlgorithmInitializedEvent event = new AlgorithmInitializedEvent();
		this.eventBus.post(event);
		this.logger.info("Starting algorithm {} with problem {} and config {}", this, this.input, this.config);
		return event;
	}

	/**
	 * This methods terminates the algorithm, setting the internal state to inactive and emitting the mandatory AlgorithmFinishedEvent over the event bus.
	 *
	 * @return The algorithm finished event.
	 */
	protected AlgorithmFinishedEvent terminate() {
		this.state = AlgorithmState.inactive;
		AlgorithmFinishedEvent finishedEvent = new AlgorithmFinishedEvent();
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
}
