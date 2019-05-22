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
import jaicore.basic.StringUtil;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
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
	protected static final String TESTEDALGORITHM_LOGGERNAME = "testedalgorithm";
	private static final int TIMEOUT_DELAY = 12000;
	private static final int TOTAL_EXPERIMENT_TIMEOUT = 20000;
	private static final int INTERRUPTION_DELAY = 5000;
	private static final int INTERRUPTION_CLEANUP_TOLERANCE = 10000; // this is the time the thread has to react upon an interrupt
	private static final int THREAD_SHUTDOWN_TOLERANCE = 2000; // this is the time until which all threads must have been shutdown after the experiment
	private static final int EARLY_TERMINATION_TOLERANCE = 50;
	private static final int MAX_TIME_TO_RETURN_CONTROL_TO_CANCELER = 200;

	// fields used together with @Parameter must be public
	@Parameter(0)
	public IAlgorithmTestProblemSet<?> problemSet;

	public abstract IAlgorithm<?, ?> getAlgorithm(Object problem) throws AlgorithmCreationException;

	@BeforeClass
	public static void initClass() {
		GlobalTimer.getInstance(); // this is to avoid that the timeout timer is spawned as a thread of a specific group
	}

	@Before
	public void initTest() throws InterruptedException {
		Thread.sleep(500);
		System.gc(); // run garbage collection
	}

	@Test
	public void testStartAndFinishEventEmissionSequentially() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmCreationException, AlgorithmTestProblemSetCreationException {
		this.checkPreconditionForTest();
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(this.problemSet.getSimpleProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		CheckingEventListener listener = new CheckingEventListener();
		algorithm.registerListener(listener);
		try {
			algorithm.call();
		} catch (AlgorithmTimeoutedException e) { // it may happen, that no solution has been found within the specified timeout. Then algorithm must, however, have emitted an event
		}
		listener.checkState();
		this.checkPreconditionForTest();
	}

	@Test
	public void testStartAndFinishEventEmissionProtocolParallelly() throws AlgorithmCreationException, AlgorithmTestProblemSetCreationException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		this.checkPreconditionForTest();
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(this.problemSet.getSimpleProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
		CheckingEventListener listener = new CheckingEventListener();
		algorithm.registerListener(listener);
		try {
			algorithm.call();
		} catch (AlgorithmTimeoutedException e) { // it may happen, that no solution has been found within the specified timeout. Then algorithm must, however, have emitted an event
		}
		listener.checkState();
		this.checkPreconditionForTest();
	}

	@Test
	public void testStartAndFinishEventEmissionByIteration() throws AlgorithmCreationException, AlgorithmTestProblemSetCreationException {
		this.checkPreconditionForTest();

		IAlgorithm<?, ?> algorithm = this.getAlgorithm(this.problemSet.getSimpleProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		CheckingEventListener listener = new CheckingEventListener();
		try {
			for (AlgorithmEvent e : algorithm) {
				listener.receiveEvent(e);
			}
		} catch (ExceptionInAlgorithmIterationException e) {
			if (e.getCause() instanceof AlgorithmTimeoutedException) {
				this.logger.warn("Algorithm has been timeouted. Cannot safely check that a finished event would have been returned.");
				listener.receiveEvent(new AlgorithmFinishedEvent(algorithm.getId())); // pretend that the algorithm would have send an AlgorithmFinishedEvent
			} else {
				throw e;
			}
		}
		listener.checkState();
		this.checkPreconditionForTest();
	}

	@Test
	public void testInterrupt() throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.runInterruptTest(false);
	}

	@Test
	public void testInterruptWhenParallelized() throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.runInterruptTest(true);
	}

	@Test
	public void testCancel() throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.runCancelTest(false);
	}

	@Test
	public void testCancelWhenParallelized() throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.runCancelTest(true);
	}

	@Test
	public void testTimeout() throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		try {
			this.runTimeoutTest(false);
		}
		finally {

		}
	}

	@Test
	public void testTimeoutWhenParallelized() throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.runTimeoutTest(true);
	}

	public void runInterruptTest(final boolean parallelized) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.checkPreconditionForTest();

		/* set up algorithm */
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(this.problemSet.getDifficultProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Testing interruptibility of algorithm {} ({}) with problem input {}", algorithm.getId(), algorithm.getClass().getName(), StringUtil.toStringLimited(algorithm.getInput(), 100));
		}
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		algorithm.setTimeout(1, TimeUnit.DAYS); // effectively deactivate timeout that has maybe been set during construction
		if (parallelized) {
			algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
			algorithm.setMaxNumThreads(Runtime.getRuntime().availableProcessors());
		}
		FutureTask<?> task = new FutureTask<>(algorithm);

		/* prepare algorithm thread with a new thread group so that the algorithm can be monitored more easily */
		AtomicLong start = new AtomicLong();
		boolean controlledInterruptedExceptionSeen = false;
		ThreadGroup algorithmThreadGroup = new ThreadGroup("InterruptTestGroup");
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
				GeneralAlgorithmTester.this.logger.info("Interrupting thread {} after {}ms", algorithmThread, now - start.get());
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
			this.logger.warn("Time limit for test has been reached.");
		} finally {
			threadCountObserverThread.cancel();
		}
		int runtime = (int) (System.currentTimeMillis() - start.get());
		int reactionTime = interruptEvent.get() > 0 ? (int) (System.currentTimeMillis() - interruptEvent.get()) : Integer.MAX_VALUE;
		this.logger.info("Executing thread has returned control after {}ms. Reaction time was {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime, reactionTime);
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY - EARLY_TERMINATION_TOLERANCE);
		assertTrue("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after the interrupt.", reactionTime <= INTERRUPTION_CLEANUP_TOLERANCE);
		assertTrue("The algorithm has not emitted an interrupted exception.", controlledInterruptedExceptionSeen);

		/*
		 * now sending a cancel to make sure the algorithm structure is shutdown
		 * (this is because the interrupt only requires that the executing thread
		 * is returned but not that the algorithm is shutdown
		 */
		algorithm.cancel();
		this.waitForThreadGroupToBecomeEmpty(algorithmThreadGroup);
		this.checkPreconditionForTest();
		this.logger.info("Interrupt-Test finished.");
	}

	public void runCancelTest(final boolean parallelized) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.checkPreconditionForTest();

		/* set up algorithm */
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(this.problemSet.getDifficultProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Testing cancel of algorithm {} ({}) with problem input {}", algorithm.getId(), algorithm.getClass().getName(), StringUtil.toStringLimited(algorithm.getInput(), 100));
		}
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
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
			this.logger.warn("Time limit for test has been reached.");
		} finally {
			threadCountObserverThread.cancel();
			timer.cancel();
		}
		int runtime = (int) (System.currentTimeMillis() - start.get());
		int reactionTime = cancelEvent.get() > 0 ? (int) (System.currentTimeMillis() - cancelEvent.get()) : Integer.MAX_VALUE;
		assertFalse("Thread must not be interrupted after cancel!", Thread.currentThread().isInterrupted());
		assertTrue("The cancel command blocked the thread for " + timeRequiredToProcessCancel + "ms, but only " + MAX_TIME_TO_RETURN_CONTROL_TO_CANCELER + " are allowed.",
				timeRequiredToProcessCancel.get() <= MAX_TIME_TO_RETURN_CONTROL_TO_CANCELER);
		this.logger.info("Executing thread has returned control after {}ms. Reaction time was {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime, reactionTime);
		assertTrue("The number of threads used during execution reached " + threadCountObserverThread.getMaxObservedThreads() + " while allowed maximum is " + allowedCPUs + ". Observed threads: \n\t- " + Arrays
				.asList(threadCountObserverThread.getThreadsAtPointOfViolation() != null ? threadCountObserverThread.getThreadsAtPointOfViolation() : new Thread[0]).stream().map(Thread::getName).collect(Collectors.joining("\n\t- ")),
				!threadCountObserverThread.isThreadConstraintViolated());
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY - EARLY_TERMINATION_TOLERANCE);
		assertTrue("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after it has been canceled.", reactionTime <= INTERRUPTION_CLEANUP_TOLERANCE);
		assertTrue("The algorithm has not emitted an AlgorithmExecutionCanceledException.", cancellationExceptionSeen);
		this.waitForThreadGroupToBecomeEmpty(algorithmThreadGroup);
		this.checkPreconditionForTest();
		this.logger.info("Cancel-Test finished.");
	}

	public void runTimeoutTest(final boolean parallelized) throws AlgorithmTestProblemSetCreationException, InterruptedException, ExecutionException, AlgorithmCreationException {
		this.checkPreconditionForTest();

		/* set up algorithm */
		IAlgorithm<?, ?> algorithm = this.getAlgorithm(this.problemSet.getDifficultProblemInputForGeneralTestPurposes());
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Testing timeout of algorithm {} ({}) with problem input {}", algorithm.getId(), algorithm.getClass().getName(), StringUtil.toStringLimited(algorithm.getInput(), 100));
		}
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
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
			this.logger.warn(
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
		this.logger.info("Executing thread has returned control after {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime);
		if (runtime < TIMEOUT_DELAY) {
			this.logger.warn(
					"Runtime was only {} seconds but should be at least {}. There might be a problem with the difficulty of the problem. If the algorithm is designed to exit smoothly on a timeout, you can safely ignore this warning.",
					runtime, TIMEOUT_DELAY);
		}
		assertFalse("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + " ms after the specified timeout.", timeoutTriggered);
		this.waitForThreadGroupToBecomeEmpty(tg);
		this.checkPreconditionForTest();
		this.logger.info("Timeout-Test finished.");
	}

	protected void checkPreconditionForTest() {
		assert !Thread.currentThread().isInterrupted() : "Execution thread must not be interrupted at start of test!";
		assert GlobalTimer.getInstance().getNumberOfActiveTasks() == 0 : "Global Timer has still " + GlobalTimer.getInstance().getNumberOfActiveTasks() + " active jobs: "
				+ GlobalTimer.getInstance().getActiveTasks().stream().map(t -> "\n\t" + t.toString()).collect(Collectors.joining());
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
