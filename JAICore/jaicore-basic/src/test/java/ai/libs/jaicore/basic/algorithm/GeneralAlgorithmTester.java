package ai.libs.jaicore.basic.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.algorithm.exceptions.ExceptionInAlgorithmIterationException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.basic.StringUtil;
import ai.libs.jaicore.concurrent.GlobalTimer;
import ai.libs.jaicore.concurrent.ThreadGroupObserver;
import ai.libs.jaicore.interrupt.Interrupter;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.test.MediumTest;

/**
 * A class to test any type of algorithm.
 *
 * Note that it is on purpose that this class is not generic in the input/output classes of the tested algorithm. The reason is that the algorithm is tested for different input instances, and these may also have different types. Of course,
 * the main type of the different inputs is usually the same, but since the type itself may be generic, the concrete type may depend on the input itself.
 */
public abstract class GeneralAlgorithmTester extends ATest {

	private static final int TIMEOUT_DELAY = 12000;
	private static final int TOTAL_EXPERIMENT_TIMEOUT = 20000;
	private static final int INTERRUPTION_DELAY = 5000;
	private static final int INTERRUPTION_CLEANUP_TOLERANCE = 10000; // this is the time the thread has to react upon an interrupt
	private static final int THREAD_SHUTDOWN_TOLERANCE = 2000; // this is the time until which all threads must have been shutdown after the experiment
	private static final int EARLY_TERMINATION_TOLERANCE = 50;
	private static final int MAX_TIME_TO_RETURN_CONTROL_TO_CANCELER = 2000;

	public abstract IAlgorithm<?, ?> getAlgorithm(Object problem) throws AlgorithmCreationException;

	@BeforeAll
	public static void initClass() {
		GlobalTimer.getInstance(); // this is to avoid that the timeout timer is spawned as a thread of a specific group
	}

	@ParameterizedTest(name="Single-Threaded check on start and finish event via event bus on {0}")
	@MethodSource("getProblemSets")
	public void testStartAndFinishEventEmissionSequentially(final IAlgorithmTestProblemSet<?> problemSet)
			throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmCreationException, AlgorithmTestProblemSetCreationException {
		this.checkPreconditionForTest(problemSet);
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(problemSet.getSimpleProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		}
		CheckingEventListener listener = new CheckingEventListener();
		algorithm.registerListener(listener);
		try {
			algorithm.call();
		} catch (AlgorithmTimeoutedException e) { // it may happen, that no solution has been found within the specified timeout. Then algorithm must, however, have emitted an event
		}
		listener.checkState();
		this.checkPreconditionForTest(problemSet);
	}

	@ParameterizedTest(name="Multi-Threaded check on start and finish event via event bus on {0}")
	@MethodSource("getProblemSets")
	public void testStartAndFinishEventEmissionProtocolParallelly(final IAlgorithmTestProblemSet<?> problemSet)
			throws AlgorithmCreationException, AlgorithmTestProblemSetCreationException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		this.checkPreconditionForTest(problemSet);
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(problemSet.getSimpleProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		}
		algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
		CheckingEventListener listener = new CheckingEventListener();
		algorithm.registerListener(listener);
		try {
			this.logger.info("Calling algorithm {}", algorithm.getId());
			algorithm.call();
			this.logger.info("Gained back control from algorithm {}", algorithm.getId());
		} catch (AlgorithmTimeoutedException e) { // it may happen, that no solution has been found within the specified timeout. Then algorithm must, however, have emitted an event
		}
		listener.checkState();
		this.checkPreconditionForTest(problemSet);
	}

	@ParameterizedTest(name="Check on start and finish event via iterator on {0}")
	@MethodSource("getProblemSets")
	public void testStartAndFinishEventEmissionByIteration(final IAlgorithmTestProblemSet<?> problemSet) throws AlgorithmCreationException, AlgorithmTestProblemSetCreationException, InterruptedException {
		this.checkPreconditionForTest(problemSet);

		IAlgorithm<?, ?> algorithm = this.getAlgorithm(problemSet.getSimpleProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		}
		CheckingEventListener listener = new CheckingEventListener();
		this.logger.info("Start testing that start and finish event are emitted during iteration.");
		try {
			for (IAlgorithmEvent e : algorithm) {
				listener.receiveEvent(e);
			}
		} catch (ExceptionInAlgorithmIterationException e) {
			if (e.getCause() instanceof AlgorithmTimeoutedException) {
				this.logger.warn("Algorithm has been timeouted. Cannot safely check that a finished event would have been returned.");
				listener.receiveEvent(new AlgorithmFinishedEvent(algorithm)); // pretend that the algorithm would have send an AlgorithmFinishedEvent
			} else {
				throw e;
			}
		}
		this.logger.info("Algorithm has no more events.");
		listener.checkState();
		this.checkPreconditionForTest(problemSet);
	}

	@MediumTest
	@ParameterizedTest(name="Single-Thread Interrupt test on {0}")
	@MethodSource("getProblemSets")
	public void testInterrupt(final IAlgorithmTestProblemSet<?> problemSet) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.logger.info("Testing interruptibility without parallelization for problem set {}", problemSet);
		this.runInterruptTest(problemSet, false);
	}

	@DisplayName("Test Interrupt Parallelized")
	@MediumTest
	@ParameterizedTest(name="Multi-Thread Interrupt test on {0}")
	@MethodSource("getProblemSets")
	public void testInterruptWhenParallelized(final IAlgorithmTestProblemSet<?> problemSet) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.logger.info("Testing interruptibility under parallelization for problem set {}", problemSet);
		this.runInterruptTest(problemSet, true);
	}

	@DisplayName("Test Cancel")
	@MediumTest
	@ParameterizedTest(name="Single-Thread Cancellation test on {0}")
	@MethodSource("getProblemSets")
	public void testCancel(final IAlgorithmTestProblemSet<?> problemSet) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.logger.info("Testing cancelability without parallelization for problem set {}", problemSet);
		this.runCancelTest(problemSet, false);
	}

	@DisplayName("Test Cancel Parallelized")
	@MediumTest
	@ParameterizedTest(name="Multi-Thread Cancellation test on {0}")
	@MethodSource("getProblemSets")
	public void testCancelWhenParallelized(final IAlgorithmTestProblemSet<?> problemSet) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.logger.info("Testing cancelability under parallelization for problem set {}", problemSet);
		this.runCancelTest(problemSet, true);
	}

	@DisplayName("Test Timeout")
	@MediumTest
	@ParameterizedTest(name="Single-Thread Timeout Test on {0}")
	@MethodSource("getProblemSets")
	public void testTimeout(final IAlgorithmTestProblemSet<?> problemSet) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.logger.info("Testing timeout adherence without parallelization for problem set {}", problemSet);
		this.runTimeoutTest(problemSet, false);
	}

	@DisplayName("Test Timeout Parallelized")
	@MediumTest
	@ParameterizedTest(name="Multi-Thread Timeout Test on {0}")
	@MethodSource("getProblemSets")
	public void testTimeoutWhenParallelized(final IAlgorithmTestProblemSet<?> problemSet) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.logger.info("Testing timeout adherence under parallelization for problem set {}", problemSet);
		this.runTimeoutTest(problemSet, true);
	}

	public void runInterruptTest(final IAlgorithmTestProblemSet<?> problemSet, final boolean parallelized) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.checkPreconditionForTest(problemSet);

		/* set up algorithm */
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(problemSet.getDifficultProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Testing interruptibility of algorithm {} ({}) with problem input {}", algorithm.getId(), algorithm.getClass().getName(), StringUtil.toStringLimited(algorithm.getInput(), 100));
		}
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		}
		algorithm.setTimeout(1, TimeUnit.DAYS); // effectively deactivate timeout that has maybe been set during construction
		if (parallelized) {
			algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
			algorithm.setMaxNumThreads(-1);
		}
		FutureTask<?> task = new FutureTask<>(algorithm);

		/* prepare algorithm thread with a new thread group so that the algorithm can be monitored more easily */
		AtomicLong start = new AtomicLong();
		boolean controlledInterruptedExceptionSeen = false;
		ThreadGroup algorithmThreadGroup = new ThreadGroup("InterruptTestGroup");
		Thread algorithmThread = new Thread(algorithmThreadGroup, task, "InterruptTest Algorithm runner for " + algorithm.getId());
		AtomicBoolean threadNumberViolated = new AtomicBoolean();
		AtomicLong interruptEvent = new AtomicLong();
		ThreadGroupObserver threadCountObserverThread = null;
		if (!parallelized) {
			threadCountObserverThread = new ThreadGroupObserver(algorithmThreadGroup, algorithm.getConfig().threads(), () -> {
				threadNumberViolated.set(true);
				algorithm.cancel();
			});
			threadCountObserverThread.start();
		}

		/* set up timer for interruption */
		String interruptReason = "testing interrupt from the outside";
		start.set(System.currentTimeMillis());
		new Timer("InterruptTest Timer").schedule(new TimerTask() {
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				GeneralAlgorithmTester.this.logger.info("Interrupting thread {} after {}ms", algorithmThread, now - start.get());
				interruptEvent.set(now);
				Interrupter.get().interruptThread(algorithmThread, interruptReason);
			}
		}, INTERRUPTION_DELAY);

		/* launch algorithm */
		algorithmThread.start();
		boolean finishedEarly = false;
		try {
			task.get(TOTAL_EXPERIMENT_TIMEOUT, TimeUnit.MILLISECONDS);
			this.logger.warn("Algorithm terminated without exception but with regular output. Maybe the benchmark is too easy.");
			finishedEarly = true;
		} catch (ExecutionException e) {
			if (e.getCause() instanceof InterruptedException && Interrupter.get().hasThreadBeenInterruptedWithReason(algorithmThread, interruptReason)) {
				controlledInterruptedExceptionSeen = true;
			} else if (e.getCause() instanceof AlgorithmExecutionCanceledException && threadNumberViolated.get()) {
				this.logger.info("Detected thread count violation.");
			} else {
				throw e;
			}
		} catch (TimeoutException e) {
			this.logger.warn("Time limit for test has been reached.");
		} finally {
			if (!parallelized) {
				threadCountObserverThread.cancel();
			}
		}
		int runtime = (int) (System.currentTimeMillis() - start.get());
		int reactionTime = interruptEvent.get() > 0 ? (int) (System.currentTimeMillis() - interruptEvent.get()) : 0;
		this.logger.info("Executing thread has returned control after {}ms. Reaction time was {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime, reactionTime);
		if (runtime < INTERRUPTION_DELAY - EARLY_TERMINATION_TOLERANCE) {
			this.logger.warn("Runtime was {}ms and hence less than {}ms, actually should be at least 10 seconds.", runtime, INTERRUPTION_DELAY - EARLY_TERMINATION_TOLERANCE);
		}
		assertTrue(reactionTime <= INTERRUPTION_CLEANUP_TOLERANCE, "The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after the interrupt.");
		if (!finishedEarly) {
			assertTrue(controlledInterruptedExceptionSeen, "The algorithm has not emitted an interrupted exception.");
		}

		/*
		 * now sending a cancel to make sure the algorithm structure is shutdown
		 * (this is because the interrupt only requires that the executing thread
		 * is returned but not that the algorithm is shutdown
		 */
		algorithm.cancel();
		this.logger.info("Waiting for algorithm thread to die.");
		algorithmThread.join();
		this.logger.info("Ready.");
		this.waitForThreadGroupToBecomeEmpty(algorithmThreadGroup);
		this.checkPreconditionForTest(problemSet);
		this.logger.info("Interrupt-Test finished.");
	}

	public void runCancelTest(final IAlgorithmTestProblemSet<?> problemSet, final boolean parallelized) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.checkPreconditionForTest(problemSet);

		/* set up algorithm */
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(problemSet.getDifficultProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Testing cancel of algorithm {} ({}) with problem input {}", algorithm.getId(), algorithm.getClass().getName(), StringUtil.toStringLimited(algorithm.getInput(), 100));
		}
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		}
		algorithm.setTimeout(1, TimeUnit.DAYS); // effectively deactivate timeout that has maybe been set during construction
		int allowedCPUs = parallelized ? Runtime.getRuntime().availableProcessors() : 1;
		if (parallelized) {
			algorithm.setNumCPUs(allowedCPUs);
			algorithm.setMaxNumThreads(allowedCPUs);
		}
		FutureTask<?> task = new FutureTask<>(algorithm);
		AtomicLong start = new AtomicLong();

		/* set up timer for interruption */
		AtomicLong cancelEvent = new AtomicLong();
		AtomicLong timeRequiredToProcessCancel = new AtomicLong();
		Timer timer = new Timer("CancelTest Timer");
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				GeneralAlgorithmTester.this.logger.info("Triggering cancel on {} after {}ms", algorithm.getId(), now - start.get());
				cancelEvent.set(now);
				algorithm.cancel();
				timeRequiredToProcessCancel.set(System.currentTimeMillis() - now);
				GeneralAlgorithmTester.this.logger.info("Cancel fully processed after {}ms", timeRequiredToProcessCancel.get());
			}
		}, INTERRUPTION_DELAY);

		/* prepare algorithm thread with a new thread group so that the algorithm can be monitored more easily */
		boolean cancellationExceptionSeen = false;
		ThreadGroup algorithmThreadGroup = new ThreadGroup("CancelTestGroup");
		Thread algorithmThread = new Thread(algorithmThreadGroup, task, "CancelTest Algorithm runner for " + algorithm.getId());
		AtomicBoolean threadNumberViolated = new AtomicBoolean();
		ThreadGroupObserver threadCountObserverThread = null;
		if (!parallelized) {
			threadCountObserverThread = new ThreadGroupObserver(algorithmThreadGroup, algorithm.getConfig().threads(), () -> {
				threadNumberViolated.set(true);
				algorithm.cancel();
			});
			threadCountObserverThread.start();
		}

		/* launch algorithm */
		start.set(System.currentTimeMillis());
		algorithmThread.start();
		boolean finishedEarly = false;
		try {
			task.get(TOTAL_EXPERIMENT_TIMEOUT, TimeUnit.MILLISECONDS);
			finishedEarly = true;
			this.logger.info("Gained back control from test task.");
			this.logger.warn("Algorithm terminated without exception but with regular output. Maybe the benchmark is too easy.");
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
					assertTrue(ex.getDelay() <= INTERRUPTION_CLEANUP_TOLERANCE, "The algorithm has sent an AlgorithmExceutionCanceledException, which is correct, but the cancel was triggered with a delay of " + ex.getDelay()
					+ "ms, which exceeds the allowed time of " + INTERRUPTION_CLEANUP_TOLERANCE + "ms.");
				} else {
					throw e;
				}
			}
		} catch (TimeoutException e) {
			this.logger.warn("Time limit for test has been reached.");
		} finally {
			if (!parallelized) {
				threadCountObserverThread.cancel();
			}
			timer.cancel();
		}
		int runtime = (int) (System.currentTimeMillis() - start.get());
		int reactionTime = cancelEvent.get() > 0 ? (int) (System.currentTimeMillis() - cancelEvent.get()) : 0;
		assertFalse(Thread.currentThread().isInterrupted(), "Thread must not be interrupted after cancel!");
		assertTrue(timeRequiredToProcessCancel.get() <= MAX_TIME_TO_RETURN_CONTROL_TO_CANCELER,
				"The cancel command blocked the thread for " + timeRequiredToProcessCancel + "ms, but only " + MAX_TIME_TO_RETURN_CONTROL_TO_CANCELER + " are allowed.");
		this.logger.info("Executing thread has returned control after {}ms. Reaction time was {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime, reactionTime);
		if (!parallelized) {
			assertTrue(!threadCountObserverThread.isThreadConstraintViolated(),
					"The number of threads used during execution reached " + threadCountObserverThread.getMaxObservedThreads() + " while allowed maximum is " + allowedCPUs + ". Observed threads: \n\t- "
							+ Arrays.asList(threadCountObserverThread.getThreadsAtPointOfViolation() != null ? threadCountObserverThread.getThreadsAtPointOfViolation() : new Thread[0]).stream().map(Thread::getName)
							.collect(Collectors.joining("\n\t- ")));
		}
		if (runtime < INTERRUPTION_DELAY - EARLY_TERMINATION_TOLERANCE) {
			this.logger.warn("Runtime was {}ms and hence less than {}ms, actually should be at least 10 seconds.", runtime, INTERRUPTION_DELAY - EARLY_TERMINATION_TOLERANCE);
		}
		assertTrue(reactionTime <= INTERRUPTION_CLEANUP_TOLERANCE, "The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after it has been canceled.");
		if (!finishedEarly) {
			assertTrue(cancellationExceptionSeen, "The algorithm has not emitted an AlgorithmExecutionCanceledException.");
		}
		this.logger.info("Waiting for algorithm thread to die.");
		algorithmThread.join();
		this.logger.info("Ready.");
		this.waitForThreadGroupToBecomeEmpty(algorithmThreadGroup);
		this.logger.info("Cancel-Test finished.");
	}

	public void runTimeoutTest(final IAlgorithmTestProblemSet<?> problemSet, final boolean parallelized) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.checkPreconditionForTest(problemSet);

		/* set up algorithm */
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(problemSet.getDifficultProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Testing timeout of algorithm {} ({}) with problem input {}", algorithm.getId(), algorithm.getClass().getName(), StringUtil.toStringLimited(algorithm.getInput(), 100));
		}
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		}
		if (algorithm instanceof AAlgorithm) {
			this.logger.info("Setting timeout precaution offset to 5000");
			((AAlgorithm<?, ?>) algorithm).setTimeoutPrecautionOffset(5000);
		}
		int allowedCPUs = parallelized ? Runtime.getRuntime().availableProcessors() : 1;
		if (parallelized) {
			algorithm.setNumCPUs(allowedCPUs);
			algorithm.setMaxNumThreads(allowedCPUs);
			assert algorithm.getConfig().threads() == allowedCPUs;
		}
		FutureTask<?> task = new FutureTask<>(algorithm);
		Timeout to = new Timeout(TIMEOUT_DELAY, TimeUnit.MILLISECONDS);
		this.logger.info("Setting timeout of algorithm to {}ms", to.milliseconds());
		algorithm.setTimeout(to.milliseconds(), TimeUnit.MILLISECONDS);
		assert algorithm.getTimeout().equals(to) : "Algorithm timeout is " + algorithm.getTimeout() + " but " + to + " has been specified!";

		/* prepare algorithm thread with a new thread group so that the algorithm can be monitored more easily */
		boolean timeoutTriggered = false;
		ThreadGroup tg = new ThreadGroup("TimeoutTestGroup");
		Thread algorithmThread = new Thread(tg, task, "TimeoutTest Algorithm runner for " + algorithm.getId());
		AtomicBoolean threadNumberViolated = new AtomicBoolean();
		ThreadGroupObserver threadCountObserverThread = null;
		if (!parallelized) {
			threadCountObserverThread = new ThreadGroupObserver(tg, algorithm.getConfig().threads(), () -> {
				threadNumberViolated.set(true);
				algorithm.cancel();
			});
			threadCountObserverThread.start();
		}

		/* launch algorithm */
		algorithmThread.start();
		long start = System.currentTimeMillis();
		try {
			task.get(TIMEOUT_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS); // here we cancel earlier than in the other two tests, because it is the job of the algorithm to make sure that the timeout is respected
			this.logger.warn(
					"Algorithm terminated without exception but with regular output. In general, this is allowed, but if the algorithm is not specialized on safe termination under timeouts, the tested problem might just have been too easy.");
		} catch (ExecutionException e) {
			if (e.getCause() instanceof AlgorithmTimeoutedException) {
				AlgorithmTimeoutedException ex = (AlgorithmTimeoutedException) e.getCause();
				assertTrue(ex.getDelay() <= INTERRUPTION_CLEANUP_TOLERANCE,
						"The algorithm has sent a TimeoutException, which is correct, but the timeout was triggered with a delay of " + ex.getDelay() + "ms, which exceeds the allowed time of " + INTERRUPTION_CLEANUP_TOLERANCE + "ms.");
			} else if (e.getCause() instanceof AlgorithmExecutionCanceledException && threadNumberViolated.get()) {
				Thread.interrupted(); // this was a controlled interrupt, reset the flag
			} else {
				throw e;
			}
		} catch (TimeoutException e) {
			timeoutTriggered = true;
		} finally {
			if (!parallelized) {
				threadCountObserverThread.cancel();
			}
		}
		long end = System.currentTimeMillis();
		int runtime = (int) (end - start);
		assertFalse(Thread.currentThread().isInterrupted(), "Thread must not be interrupted after timeout!");
		if (!parallelized) {
			Thread[] threadsAtTimeOfViolation = threadCountObserverThread.getThreadsAtPointOfViolation();
			assertTrue(!threadCountObserverThread.isThreadConstraintViolated(), "The number of threads used during execution reached " + threadCountObserverThread.getMaxObservedThreads() + " while allowed maximum is " + allowedCPUs
					+ ". Observed threads: \n\t- " + Arrays.asList(threadsAtTimeOfViolation != null ? threadsAtTimeOfViolation : new Thread[0]).stream().map(Thread::getName).collect(Collectors.joining("\n\t- ")));
		}
		this.logger.info("Executing thread has returned control after {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime);
		if (runtime < TIMEOUT_DELAY) {
			this.logger.warn(
					"Runtime was only {} seconds but should be at least {}. There might be a problem with the difficulty of the problem. If the algorithm is designed to exit smoothly on a timeout, you can safely ignore this warning.",
					runtime, TIMEOUT_DELAY);
		}
		assertFalse(timeoutTriggered, "The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + " ms after the specified timeout.");

		/* check thread status */
		this.logger.info("Waiting for algorithm thread to die.");
		algorithmThread.join();
		this.logger.info("Ready.");
		this.waitForThreadGroupToBecomeEmpty(tg);
		this.checkPreconditionForTest(problemSet);
		this.logger.info("Timeout-Test finished.");
	}

	protected void checkPreconditionForTest(final IAlgorithmTestProblemSet<?> problemSet) throws InterruptedException {
		Objects.requireNonNull(problemSet);
	}

	private void waitForThreadGroupToBecomeEmpty(final ThreadGroup group) throws InterruptedException {
		this.logger.info("Waiting for thread group {} to become empty.", group);
		int sleepTime = 100;
		int n = THREAD_SHUTDOWN_TOLERANCE / sleepTime;
		int numberOfThreadsAfter = group.activeCount();
		for (int i = 0; i < n && numberOfThreadsAfter > 0; i++) {
			this.logger.info("Thread wait {}/{}: There are still {} threads active. Waiting {}ms for another check.", i + 1, n, numberOfThreadsAfter, sleepTime);
			Awaitility.await().atLeast(Duration.ofMillis(sleepTime));
			numberOfThreadsAfter = group.activeCount();
		}

		/* get open threads */
		Thread[] threads = new Thread[group.activeCount()];
		if (numberOfThreadsAfter > 0) {
			group.enumerate(threads, true);
		}
		assertEquals(0, numberOfThreadsAfter, "Number of threads has increased with execution. New threads: " + Arrays.toString(threads));
	}

	private class CheckingEventListener {
		boolean observedInit = false;
		boolean observedInitExactlyOnce = false;
		boolean observedInitBeforeFinish = false;
		boolean observedFinish = false;
		boolean observedFinishExactlyOnce = false;

		public void receiveEvent(final IAlgorithmEvent e) {

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
			assertTrue(this.observedInit, "No init event was observed");
			assertTrue(this.observedInitExactlyOnce, "More than one init event was observed");
			assertTrue(this.observedInitBeforeFinish, "A finish event was observed prior to an init event");
			assertTrue(this.observedFinish, "No finish event was observed");
			assertTrue(this.observedFinishExactlyOnce, "More than one finish event was observed");
		}
	}
}
