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

import org.junit.Before;
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
import jaicore.concurrent.GlobalTimer;
import jaicore.concurrent.ThreadGroupObserver;
import jaicore.interrupt.Interrupter;

/**
 * A class to test any type of algorithm.
 *
 * Note that it is on purpose that this class is not generic in the input/output classes of the tested algorithm. The reason is that the algorithm is tested for different input instances, and these may also have different types. Of course,
 * the main type of the different inputs is usually the same, but since the type itself may be generic, the concrete type may depend on the input itself.
 */

@RunWith(Parameterized.class)
public abstract class GeneralAlgorithmTester implements ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(GeneralAlgorithmTester.class);
	private static final String TESTEDALGORITHM_LOGGERNAME = "testedalgorithm";
	private static final int TIMEOUT_DELAY = 12000;
	private static final int TOTAL_EXPERIMENT_TIMEOUT = 20000;
	private static final int INTERRUPTION_DELAY = 5000;
	private static final int INTERRUPTION_CLEANUP_TOLERANCE = 10000;
	private static final int THREAD_SHUTDOWN_TOLERANCE = 2000;
	private static final int EARLY_TERMINATION_TOLERANCE = 50;
	private static final int MAX_TIME_TO_RETURN_CONTROL_TO_CANCELER = 200;

	// fields used together with @Parameter must be public
	@Parameter(0)
	public IAlgorithmTestProblemSet<?> problemSet;

	public abstract IAlgorithm<?, ?> getAlgorithm(Object problem);

	@BeforeClass
	public static void initClass() {
		GlobalTimer.getInstance(); // this is to avoid that the timeout timer is spawned as a thread of a specific group
	}

	@Before
	public void initTest() {
		System.gc(); // run garbage collection
	}

	@Test
	public void testStartAndFinishEventEmissionSequentially() throws Exception {
		IAlgorithm<?, ?> algorithm = getAlgorithm(problemSet.getSimpleProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		CheckingEventListener listener = new CheckingEventListener();
		algorithm.registerListener(listener);
		algorithm.call();
		listener.checkState();
		checkNotInterrupted();
	}

	@Test
	public void testStartAndFinishEventEmissionProtocolParallelly() throws Exception {
		IAlgorithm<?, ?> algorithm = getAlgorithm(problemSet.getSimpleProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
		CheckingEventListener listener = new CheckingEventListener();
		algorithm.registerListener(listener);
		algorithm.call();
		listener.checkState();
		checkNotInterrupted();
	}

	@Test
	public void testStartAndFinishEventEmissionByIteration() throws Exception {
		IAlgorithm<?, ?> algorithm = getAlgorithm(problemSet.getSimpleProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		CheckingEventListener listener = new CheckingEventListener();
		for (AlgorithmEvent e : algorithm) {
			listener.receiveEvent(e);
		}
		listener.checkState();
		checkNotInterrupted();
	}

	@Test
	public void testInterrupt() throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException {
		runInterruptTest(false);
	}

	@Test
	public void testInterruptWhenParallelized() throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException {
		runInterruptTest(true);
	}

	@Test
	public void testCancel() throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException {
		runCancelTest(false);
	}

	@Test
	public void testCancelWhenParallelized() throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException {
		runCancelTest(true);
	}

	@Test
	public void testTimeout() throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException {
		runTimeoutTest(false);
	}

	@Test
	public void testTimeoutWhenParallelized() throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException {
		runTimeoutTest(true);
	}

	public void runInterruptTest(final boolean parallelized) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException {

		/* set up algorithm */
		IAlgorithm<?, ?> algorithm = getAlgorithm(problemSet.getDifficultProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		logger.info("Testing interruptibility of algorithm {} ({}) with problem input {}", algorithm.getId(), algorithm.getClass().getName(), algorithm.getInput());
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		if (parallelized) {
			algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
			algorithm.setMaxNumThreads(Runtime.getRuntime().availableProcessors());
		}
		FutureTask<?> task = new FutureTask<>(algorithm);

		/* prepare algorithm thread with a new thread group so that the algorithm can be monitored more easily */
		AtomicLong start = new AtomicLong();
		boolean controlledInterruptedExceptionSeen = false;
		ThreadGroup algorithmThreadGroup = new ThreadGroup("TimeoutTestGroup");
		Thread algorithmThread = new Thread(algorithmThreadGroup, task, "InterruptTest Algorithm runner for " + algorithm.getId());
		AtomicBoolean threadNumberViolated = new AtomicBoolean();
		AtomicLong interruptEvent = new AtomicLong();
		ThreadGroupObserver threadCountObserverThread = new ThreadGroupObserver(algorithmThreadGroup, algorithm.getConfig().threads(), () -> {
			threadNumberViolated.set(true);
			algorithm.cancel();
		});
		threadCountObserverThread.start();

		/* set up timer for interruption */
		String interruptReason = "testing interrupt from the outside";
		start.set(System.currentTimeMillis());
		new Timer("InterruptTest Timer").schedule(new TimerTask() {
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				logger.info("Interrupting thread {} after {}ms", algorithmThread, now - start.get());
				interruptEvent.set(now);
				Interrupter.get().interruptThread(algorithmThread, interruptReason);
			}
		}, INTERRUPTION_DELAY);

		/* launch algorithm */
		algorithmThread.start();
		try {
			task.get(TOTAL_EXPERIMENT_TIMEOUT, TimeUnit.MILLISECONDS);
			assert false : ("Algorithm terminated without exception but with regular output.");
		} catch (ExecutionException e) {
			if (e.getCause() instanceof InterruptedException && Interrupter.get().hasThreadBeenInterruptedWithReason(algorithmThread, interruptReason)) {
				controlledInterruptedExceptionSeen = true;
			} else {
				throw e;
			}
		} catch (TimeoutException e) {
			logger.warn("Time limit for test has been reached.");
		} finally {
			threadCountObserverThread.cancel();
		}
		int runtime = (int) (System.currentTimeMillis() - start.get());
		int reactionTime = interruptEvent.get() > 0 ? (int) (System.currentTimeMillis() - interruptEvent.get()) : Integer.MAX_VALUE;
		logger.info("Executing thread has returned control after {}ms. Reaction time was {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime, reactionTime);
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY - EARLY_TERMINATION_TOLERANCE);
		assertTrue("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after the interrupt.", reactionTime <= INTERRUPTION_CLEANUP_TOLERANCE);
		assertTrue("The algorithm has not emitted an interrupted exception.", controlledInterruptedExceptionSeen);

		/*
		 * now sending a cancel to make sure the algorithm structure is shutdown (this
		 * is because the interrupt only requires that the executing thread is returned
		 * but not that the algorithm is shutdown
		 */
		algorithm.cancel();
		waitForThreadGroupToBecomeEmpty(algorithmThreadGroup);
		logger.info("Interrupt-Test finished.");
	}

	public void runCancelTest(final boolean parallelized) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException {

		/* set up algorithm */
		IAlgorithm<?, ?> algorithm = getAlgorithm(problemSet.getDifficultProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		logger.info("Testing cancel of algorithm {} ({}) with problem input {}", algorithm.getId(), algorithm.getClass().getName(), algorithm.getInput());
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		int allowedCPUs = parallelized ? Runtime.getRuntime().availableProcessors() : 1;
		if (parallelized) {
			algorithm.setNumCPUs(allowedCPUs);
			algorithm.setMaxNumThreads(allowedCPUs);
		}
		FutureTask<?> task = new FutureTask<>(algorithm);
		AtomicLong start = new AtomicLong();

		/* set up timer for interruption */
		AtomicLong cancelEvent = new AtomicLong();
		new Timer("CancelTest Timer").schedule(new TimerTask() {
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				logger.info("Triggering cancel on {} after {}ms", algorithm.getId(), now - start.get());
				cancelEvent.set(now);
				algorithm.cancel();
				long timeRequiredToProcessCancel = System.currentTimeMillis() - now;
				logger.info("Cancel fully processed after {}ms", timeRequiredToProcessCancel);
				assertTrue("The cancel command blocked the thread for " + timeRequiredToProcessCancel + "ms, but only " + MAX_TIME_TO_RETURN_CONTROL_TO_CANCELER + " are allowed.", timeRequiredToProcessCancel <= MAX_TIME_TO_RETURN_CONTROL_TO_CANCELER);
			}
		}, INTERRUPTION_DELAY);

		/* prepare algorithm thread with a new thread group so that the algorithm can be monitored more easily */
		boolean cancellationExceptionSeen = false;
		ThreadGroup algorithmThreadGroup = new ThreadGroup("TimeoutTestGroup");
		Thread algorithmThread = new Thread(algorithmThreadGroup, task, "CancelTest Algorithm runner for " + algorithm.getId());
		AtomicBoolean threadNumberViolated = new AtomicBoolean();
		ThreadGroupObserver threadCountObserverThread = new ThreadGroupObserver(algorithmThreadGroup, algorithm.getConfig().threads(), () -> {
			threadNumberViolated.set(true);
			algorithm.cancel();
		});
		threadCountObserverThread.start();

		/* launch algorithm */
		start.set(System.currentTimeMillis());
		algorithmThread.start();
		try {
			Object output = task.get(TOTAL_EXPERIMENT_TIMEOUT, TimeUnit.MILLISECONDS);
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
					assertTrue("The algorithm has sent an AlgorithmExceutionCanceledException, which is correct, but the cancel was triggered with a delay of " + ex.getDelay() + "ms, which exceeds the allowed time of "
							+ INTERRUPTION_CLEANUP_TOLERANCE + "ms.", ex.getDelay() <= INTERRUPTION_CLEANUP_TOLERANCE);
				} else {
					throw e;
				}
			}
		} catch (TimeoutException e) {
			logger.warn("Time limit for test has been reached.");
		} finally {
			threadCountObserverThread.cancel();
		}
		int runtime = (int) (System.currentTimeMillis() - start.get());
		int reactionTime = cancelEvent.get() > 0 ? (int) (System.currentTimeMillis() - cancelEvent.get()) : Integer.MAX_VALUE;
		assertFalse("Thread must not be interrupted after cancel!", Thread.currentThread().isInterrupted());
		logger.info("Executing thread has returned control after {}ms. Reaction time was {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime, reactionTime);
		assertTrue("The number of threads used during execution reached " + threadCountObserverThread.getMaxObservedThreads() + " while allowed maximum is " + allowedCPUs + ". Observed threads: \n\t- " + Arrays
				.asList(threadCountObserverThread.getThreadsAtPointOfViolation() != null ? threadCountObserverThread.getThreadsAtPointOfViolation() : new Thread[0]).stream().map(Thread::getName).collect(Collectors.joining("\n\t- ")),
				!threadCountObserverThread.isThreadConstraintViolated());
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY - EARLY_TERMINATION_TOLERANCE);
		assertTrue("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after it has been canceled.", reactionTime <= INTERRUPTION_CLEANUP_TOLERANCE);
		assertTrue("The algorithm has not emitted an AlgorithmExecutionCanceledException.", cancellationExceptionSeen);
		waitForThreadGroupToBecomeEmpty(algorithmThreadGroup);
		checkNotInterrupted();
		logger.info("Cancel-Test finished.");
	}

	public void runTimeoutTest(final boolean parallelized) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException {

		/* set up algorithm */
		IAlgorithm<?, ?> algorithm = getAlgorithm(problemSet.getDifficultProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		logger.info("Testing timeoutof algorithm {} ({}) with problem input {}", algorithm.getId(), algorithm.getClass().getName(), algorithm.getInput());
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		int allowedCPUs = parallelized ? Runtime.getRuntime().availableProcessors() : 1;
		if (parallelized) {
			algorithm.setNumCPUs(allowedCPUs);
			algorithm.setMaxNumThreads(allowedCPUs);
			assert algorithm.getConfig().threads() == allowedCPUs;
		};
		FutureTask<?> task = new FutureTask<>(algorithm);
		TimeOut to = new TimeOut(TIMEOUT_DELAY, TimeUnit.MILLISECONDS);
		algorithm.setTimeout(to.milliseconds(), TimeUnit.MILLISECONDS);
		assert algorithm.getTimeout().equals(to) : "Algorithm timeout is " + algorithm.getTimeout() + " but " + to + " has been specified!";

		/* prepare algorithm thread with a new thread group so that the algorithm can be monitored more easily */
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
		long start = System.currentTimeMillis();
		try {
			task.get(TIMEOUT_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS); // here we cancel earlier than in the other two tests, because it is the job of the algorithm to make sure that the timeout is respected
			logger.warn(
					"Algorithm terminated without exception but with regular output. In general, this is allowed, but if the algorithm is not specialized on safe termination under timeouts, the tested problem might just have been too easy.");
		} catch (ExecutionException e) {
			if (e.getCause() instanceof AlgorithmTimeoutedException) {
				AlgorithmTimeoutedException ex = (AlgorithmTimeoutedException) e.getCause();
				assertTrue("The algorithm has sent a TimeoutException, which is correct, but the timeout was triggered with a delay of " + ex.getDelay() + "ms, which exceeds the allowed time of " + INTERRUPTION_CLEANUP_TOLERANCE + "ms.",
						ex.getDelay() <= INTERRUPTION_CLEANUP_TOLERANCE);
			} else if (e.getCause() instanceof AlgorithmExecutionCanceledException && threadNumberViolated.get()) {
				Thread.interrupted(); // this was a controlled interrupt, reset the flag
			} else {
				throw e;
			}
		} catch (TimeoutException e) {
			timeoutTriggered = true;
		} finally {
			threadCountObserverThread.cancel();
		}
		long end = System.currentTimeMillis();
		int runtime = (int) (end - start);
		assertFalse("Thread must not be interrupted after timeout!", Thread.currentThread().isInterrupted());
		assertTrue("The number of threads used during execution reached " + threadCountObserverThread.getMaxObservedThreads() + " while allowed maximum is " + allowedCPUs + ". Observed threads: \n\t- " + Arrays
				.asList(threadCountObserverThread.getThreadsAtPointOfViolation() != null ? threadCountObserverThread.getThreadsAtPointOfViolation() : new Thread[0]).stream().map(Thread::getName).collect(Collectors.joining("\n\t- ")),
				!threadCountObserverThread.isThreadConstraintViolated());
		logger.info("Executing thread has returned control after {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime);
		if (runtime < TIMEOUT_DELAY) {
			logger.warn(
					"Runtime was only {} seconds but should be at least {}. There might be a problem with the difficulty of the problem. If the algorithm is designed to exit smoothly on a timeout, you can safely ignore this warning.",
					runtime, INTERRUPTION_DELAY);
		}
		assertFalse("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + " ms after the specified timeout.", timeoutTriggered);
		waitForThreadGroupToBecomeEmpty(tg);
		checkNotInterrupted();
		logger.info("Timeout-Test finished.");
	}

	private void checkNotInterrupted() {
		assertTrue("Executing thread is interrupted, which must not be the case!", !Thread.currentThread().isInterrupted());
	}

	private void waitForThreadGroupToBecomeEmpty(final ThreadGroup group) throws InterruptedException {
		logger.info("Waiting for thread group {} to become empty.", group);
		int sleepTime = 100;
		int n = THREAD_SHUTDOWN_TOLERANCE / sleepTime;
		int numberOfThreadsAfter = group.activeCount();
		for (int i = 0; i < n && numberOfThreadsAfter > 0; i++) {
			logger.info("Thread wait {}/{}: There are still {} threads active. Waiting {}ms for another check.", i + 1, n, numberOfThreadsAfter, sleepTime);
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
			if (!observedInit) {
				observedInit = true;
				observedInitExactlyOnce = true;
				if (!observedFinish) {
					observedInitBeforeFinish = true;
				}
			} else {
				observedInitExactlyOnce = false;
			}
		}

		@Subscribe
		public void receiveEvent(final AlgorithmFinishedEvent e) {
			if (!observedFinish) {
				observedFinish = true;
				observedFinishExactlyOnce = true;
			} else {
				observedFinishExactlyOnce = false;
			}
		}

		void checkState() {
			assertTrue("No init event was observed", observedInit);
			assertTrue("More than one init event was observed", observedInitExactlyOnce);
			assertTrue("A finish event was observed prior to an init event", observedInitBeforeFinish);
			assertTrue("No finish event was observed", observedFinish);
			assertTrue("More than one finish event was observed", observedFinishExactlyOnce);
		}
	}

	public IAlgorithmTestProblemSet<?> getProblemSet() {
		return problemSet;
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}

	protected Logger getLogger() {
		return logger;
	}

	@Override
	public void setLoggerName(final String name) {
		logger.info("Switching logger name from {} to {}.", loggerName, name);
		loggerName = name;
		logger = LoggerFactory.getLogger(loggerName);
		logger.info("Switched logger name to {}.", loggerName);
	}

}
