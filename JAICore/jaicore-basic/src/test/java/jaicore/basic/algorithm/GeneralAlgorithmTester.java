package jaicore.basic.algorithm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.concurrent.ThreadGroupObserver;
import jaicore.concurrent.TimeoutTimer;
import jaicore.interrupt.Interrupter;

/**
 * A class to test any type of algorithm.
 *
 * Note that it is on purpose that this class is not generic in the input/output classes of the tested algorithm.
 * The reason is that the algorithm is tested for different input instances, and these may also have different types.
 * Of course, the main type of the different inputs is usually the same, but since the type itself may be generic,
 * the concrete type may depend on the input itself.
 */

@RunWith(Parameterized.class)
public abstract class GeneralAlgorithmTester implements ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(GeneralAlgorithmTester.class);
	private static final String TESTEDALGORITHM_LOGGERNAME = "testedalgorithm";
	private static final int INTERRUPTION_DELAY = 5000;
	private static final int INTERRUPTION_CLEANUP_TOLERANCE = 10000;
	private static final int THREAD_SHUTDOWN_TOLERANCE = 10000;

	// fields used together with @Parameter must be public
	@Parameter(0)
	public IAlgorithmTestProblemSet<?> problemSet;

	public abstract IAlgorithm<?, ?> getAlgorithm(Object problem);

	@BeforeClass
	public static void init() {
		TimeoutTimer.getInstance(); // this is to avoid that the timeout timer is spawned as a thread of a specific group
	}

	@Test
	public void testStartAndFinishEventEmissionSequentially() throws Exception {
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(this.problemSet.getSimpleProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		CheckingEventListener listener = new CheckingEventListener();
		algorithm.registerListener(listener);
		algorithm.call();
		listener.checkState();
		this.checkNotInterrupted();
	}

	@Test
	public void testStartAndFinishEventEmissionProtocolParallelly() throws Exception {
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(this.problemSet.getSimpleProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
		CheckingEventListener listener = new CheckingEventListener();
		algorithm.registerListener(listener);
		algorithm.call();
		listener.checkState();
		this.checkNotInterrupted();
	}

	@Test
	public void testStartAndFinishEventEmissionByIteration() throws Exception {
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(this.problemSet.getSimpleProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		CheckingEventListener listener = new CheckingEventListener();
		for (AlgorithmEvent e : algorithm) {
			listener.receiveEvent(e);
		}
		listener.checkState();
		this.checkNotInterrupted();
	}

	@Test
	public void testInterrupt() throws Exception {

		/* set up algorithm */
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(this.problemSet.getDifficultProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
		algorithm.setMaxNumThreads(Runtime.getRuntime().availableProcessors());
		FutureTask<?> task = new FutureTask<>(algorithm);

		/* prepare algorithm thread with a new thread group so that the algorithm can be monitored more easily */
		long start = System.currentTimeMillis();
		boolean controlledInterruptedExceptionSeen = false;
		boolean timeoutTriggered = false;
		ThreadGroup algorithmThreadGroup = new ThreadGroup("TimeoutTestGroup");
		Thread algorithmThread = new Thread(algorithmThreadGroup, task, "InterruptTest Algorithm runner for " + algorithm.getId());
		AtomicBoolean threadNumberViolated = new AtomicBoolean();
		ThreadGroupObserver threadCountObserverThread = new ThreadGroupObserver(algorithmThreadGroup, algorithm.getConfig().threads(), () -> {
			threadNumberViolated.set(true);
			algorithm.cancel();
		});
		threadCountObserverThread.start();

		/* set up timer for interruption */
		String interruptReason = "testing interrupt from the outside";
		algorithmThread.start();
		new Timer("InterruptTest Timer").schedule(new TimerTask() {
			@Override
			public void run() {
				GeneralAlgorithmTester.this.logger.info("Interrupting thread {}", algorithmThread);
				Interrupter.get().interruptThread(algorithmThread, interruptReason);
			}
		}, INTERRUPTION_DELAY);

		/* launch algorithm */
		try {
			task.get(INTERRUPTION_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS);
			assert false : ("Algorithm terminated without exception but with regular output.");
		} catch (ExecutionException e) {
			if (e.getCause() instanceof InterruptedException && Interrupter.get().hasThreadBeenInterruptedWithReason(algorithmThread, interruptReason)) {
				controlledInterruptedExceptionSeen = true;
			}
			else {
				throw e;
			}
		} catch (TimeoutException e) {
			timeoutTriggered = true;
		}
		int runtime = (int) (System.currentTimeMillis() - start);
		this.logger.info("Executing thread has returned control after {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime);
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY);
		assertFalse("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after the interrupt.", timeoutTriggered);
		assertTrue("The algorithm has not emitted an interrupted exception.", controlledInterruptedExceptionSeen);

		/*
		 * now sending a cancel to make sure the algorithm structure is shutdown (this
		 * is because the interrupt only requires that the executing thread is returned
		 * but not that the algorithm is shutdown
		 */
		algorithm.cancel();
		this.waitForThreadGroupToBecomeEmpty(algorithmThreadGroup);
		this.logger.info("Interrupt-Test finished.");
	}

	@Test
	public void testCancel() throws Exception {

		/* set up algorithm */
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(this.problemSet.getDifficultProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		int availableCPUs = Runtime.getRuntime().availableProcessors();
		algorithm.setNumCPUs(availableCPUs);
		algorithm.setMaxNumThreads(availableCPUs);
		FutureTask<?> task = new FutureTask<>(algorithm);

		/* set up timer for interruption */
		AtomicLong cancelEvent = new AtomicLong();
		new Timer("CancelTest Timer").schedule(new TimerTask() {
			@Override
			public void run() {
				GeneralAlgorithmTester.this.logger.info("Triggering cancel on {}", algorithm.getId());
				algorithm.cancel();
				cancelEvent.set(System.currentTimeMillis());
			}
		}, INTERRUPTION_DELAY);

		/* prepare algorithm thread with a new thread group so that the algorithm can be monitored more easily */
		long start = System.currentTimeMillis();
		boolean cancellationExceptionSeen = false;
		boolean timeoutTriggered = false;
		ThreadGroup algorithmThreadGroup = new ThreadGroup("TimeoutTestGroup");
		Thread algorithmThread = new Thread(algorithmThreadGroup, task, "CancelTest Algorithm runner for " + algorithm.getId());
		AtomicBoolean threadNumberViolated = new AtomicBoolean();
		ThreadGroupObserver threadCountObserverThread = new ThreadGroupObserver(algorithmThreadGroup, algorithm.getConfig().threads(), () -> {
			threadNumberViolated.set(true);
			algorithm.cancel();
		});
		threadCountObserverThread.start();

		/* launch algorithm */
		algorithmThread.start();
		try {
			Object output = task.get(INTERRUPTION_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS);
			assert false : ("Algorithm terminated without exception but with regular output: " + output);
		} catch (ExecutionException e) {


			/* if the max number of threads has been violated, reset interrupted flag */
			if (threadNumberViolated.get()) {
				Thread.interrupted(); // this was a controlled interrupt, reset the flag
			}

			/* otherwise perform standard check*/
			else {
				if (e.getCause() instanceof AlgorithmExecutionCanceledException) {
					AlgorithmExecutionCanceledException ex = (AlgorithmExecutionCanceledException) e.getCause();
					cancellationExceptionSeen = true;
					assertTrue ("The algorithm has sent an AlgorithmExceutionCanceledException, which is correct, but the cancel was triggered with a delay of " + ex.getDelay() + "ms, which exceeds the allowed time of 2000ms.", ex.getDelay() <= 2000);
				}
				else {
					throw e;
				}
			}
		} catch (TimeoutException e) {
			timeoutTriggered = true;
		}
		int runtime = (int) (System.currentTimeMillis() - start);
		assertFalse("Thread must not be interrupted after cancel!", Thread.currentThread().isInterrupted());
		threadCountObserverThread.cancel();
		this.logger.info("Executing thread has returned control after {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime);
		assertTrue("The number of threads used during execution reached " + threadCountObserverThread.getMaxObservedThreads() + " while allowed maximum is " + availableCPUs + ". Observed threads: \n\t- " + Arrays
				.asList(threadCountObserverThread.getThreadsAtPointOfViolation() != null ? threadCountObserverThread.getThreadsAtPointOfViolation() : new Thread[0]).stream().map(Thread::getName).collect(Collectors.joining("\n\t- ")),
				!threadCountObserverThread.isThreadConstraintViolated());
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY);
		assertFalse("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after it has been canceled.", timeoutTriggered);
		assertTrue("The algorithm has not emitted an AlgorithmExecutionCanceledException.", cancellationExceptionSeen);
		this.waitForThreadGroupToBecomeEmpty(algorithmThreadGroup);
		this.checkNotInterrupted();
		this.logger.info("Cancel-Test finished.");
	}

	@Test
	public void testQuickTimeout() throws Exception {

		/* set up algorithm */
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(this.problemSet.getDifficultProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		int availableCPUs = Runtime.getRuntime().availableProcessors();
		algorithm.setNumCPUs(availableCPUs);
		algorithm.setMaxNumThreads(availableCPUs);
		assert algorithm.getConfig().threads() == availableCPUs;
		FutureTask<?> task = new FutureTask<>(algorithm);
		TimeOut to = new TimeOut(INTERRUPTION_DELAY, TimeUnit.MILLISECONDS);
		algorithm.setTimeout(to.milliseconds(), TimeUnit.MILLISECONDS);
		assert algorithm.getTimeout().equals(to) : "Algorithm timeout is " + algorithm.getTimeout() + " but " + to + " has been specified!";

		/* prepare algorithm thread with a new thread group so that the algorithm can be monitored more easily */
		long start = System.currentTimeMillis();
		boolean timeoutTriggered = false;
		ThreadGroup tg = new ThreadGroup("TimeoutTestGroup");
		Thread algorithmThread = new Thread(tg, task, "TimeoutTest Algorithm runner for " + algorithm.getId());
		AtomicBoolean threadNumberViolated = new AtomicBoolean();
		ThreadGroupObserver threadCountObserverThread = new ThreadGroupObserver(tg, algorithm.getConfig().threads(), () -> {
			threadNumberViolated.set(true);
			algorithm.cancel();
		});
		threadCountObserverThread.start();

		/* launch algorithm */
		algorithmThread.start();
		try {
			task.get(INTERRUPTION_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS);
			this.logger.warn("Algorithm terminated without exception but with regular output. In general, this is allowed, but if the algorithm is not specialized on safe termination under timeouts, the tested problem might just have been too easy.");
		} catch (ExecutionException e) {
			if (e.getCause() instanceof AlgorithmTimeoutedException) {
				AlgorithmTimeoutedException ex = (AlgorithmTimeoutedException) e.getCause();
				assertTrue ("The algorithm has sent a TimeoutException, which is correct, but the timeout was triggered with a delay of " + ex.getDelay() + "ms, which exceeds the allowed time of 2000ms.", ex.getDelay() <= 2000);
			} else if (e.getCause() instanceof AlgorithmExecutionCanceledException && threadNumberViolated.get()) {
				Thread.interrupted(); // this was a controlled interrupt, reset the flag
			} else {
				throw e;
			}
		} catch (TimeoutException e) {
			timeoutTriggered = true;
		}
		long end = System.currentTimeMillis();
		int runtime = (int) (end - start);
		assertFalse("Thread must not be interrupted after timeout!", Thread.currentThread().isInterrupted());
		threadCountObserverThread.cancel();
		assertTrue("The number of threads used during execution reached " + threadCountObserverThread.getMaxObservedThreads() + " while allowed maximum is " + availableCPUs + ". Observed threads: \n\t- " + Arrays
				.asList(threadCountObserverThread.getThreadsAtPointOfViolation() != null ? threadCountObserverThread.getThreadsAtPointOfViolation() : new Thread[0]).stream().map(Thread::getName).collect(Collectors.joining("\n\t- ")),
				!threadCountObserverThread.isThreadConstraintViolated());
		this.logger.info("Executing thread has returned control after {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime);
		if (runtime < INTERRUPTION_DELAY) {
			this.logger.warn("Runtime was only {} seconds but should be at least {}. There might be a problem with the difficulty of the problem. If the algorithm is designed to exit smoothly on a timeout, you can safely ignore this warning.", runtime, INTERRUPTION_DELAY);
		}
		assertFalse("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + " ms after the specified timeout.", timeoutTriggered);
		this.waitForThreadGroupToBecomeEmpty(tg);
		this.checkNotInterrupted();
		this.logger.info("Timeout-Test finished.");
	}

	private void checkNotInterrupted() {
		assertTrue("Executing thread is interrupted, which must not be the case!", !Thread.currentThread().isInterrupted());
	}

	private void waitForThreadGroupToBecomeEmpty(final ThreadGroup group) throws InterruptedException {
		this.logger.info("Waiting for thread group {} to become empty.", group);
		int sleepTime = 100;
		int n = THREAD_SHUTDOWN_TOLERANCE / sleepTime;
		int numberOfThreadsAfter = group.activeCount();
		for (int i = 0; i < n && numberOfThreadsAfter > 0; i++) {
			this.logger.info("Thread wait {}/{}: There are still {} threads active. Waiting {}ms for another check.", i + 1, n, numberOfThreadsAfter, sleepTime);
			Thread.sleep(sleepTime);
			numberOfThreadsAfter = group.activeCount();
		}

		/* get open threads */
		Thread[] threads = new Thread[group.activeCount()];
		if (numberOfThreadsAfter > 0) {
			group.enumerate(threads, true);
		}
		assertTrue("Number of threads has increased with execution. New threads: " + Arrays.toString(threads), numberOfThreadsAfter == 0);
	}

	private class CheckingEventListener {
		boolean observedInit = false;
		boolean observedInitExactlyOnce = false;
		boolean observedInitBeforeFinish = false;
		boolean observedFinish = false;
		boolean observedFinishExactlyOnce = false;

		public void receiveEvent(final AlgorithmEvent e) {

			if (e instanceof AlgorithmInitializedEvent) {
				this.receiveEvent((AlgorithmInitializedEvent) e);
			} else if (e instanceof AlgorithmFinishedEvent) {
				this.receiveEvent((AlgorithmFinishedEvent) e);
			}

			/* ignore other events */
		}

		@Subscribe
		public void receiveEvent(final AlgorithmInitializedEvent e) {
			if (!this.observedInit) {
				this.observedInit = true;
				this.observedInitExactlyOnce = true;
				if (!this.observedFinish) {
					this.observedInitBeforeFinish = true;
				}
			} else {
				this.observedInitExactlyOnce = false;
			}
		}

		@Subscribe
		public void receiveEvent(final AlgorithmFinishedEvent e) {
			if (!this.observedFinish) {
				this.observedFinish = true;
				this.observedFinishExactlyOnce = true;
			} else {
				this.observedFinishExactlyOnce = false;
			}
		}

		void checkState() {
			assertTrue("No init event was observed", this.observedInit);
			assertTrue("More than one init event was observed", this.observedInitExactlyOnce);
			assertTrue("A finish event was observed prior to an init event", this.observedInitBeforeFinish);
			assertTrue("No finish event was observed", this.observedFinish);
			assertTrue("More than one finish event was observed", this.observedFinishExactlyOnce);
		}
	}



	public IAlgorithmTestProblemSet<?> getProblemSet() {
		return this.problemSet;
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	protected Logger getLogger() {
		return this.logger;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger name from {} to {}.", this.loggerName, name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(this.loggerName);
		this.logger.info("Switched logger name to {}.", this.loggerName);
	}

}
