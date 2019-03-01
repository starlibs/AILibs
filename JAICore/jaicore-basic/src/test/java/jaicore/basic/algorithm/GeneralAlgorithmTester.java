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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.concurrent.ThreadGroupObserver;

/**
 *
 * @param <P>
 *            The class of the actual problem to be solved
 * @param <I>
 *            The class of the algorithm input
 * @param <O>
 *            The class of the algorithm output
 */
public abstract class GeneralAlgorithmTester<P, I, O> implements ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(GeneralAlgorithmTester.class);
	private static final String TESTEDALGORITHM_LOGGERNAME = "testedalgorithm";
	private static final int INTERRUPTION_DELAY = 5000;
	private static final int INTERRUPTION_CLEANUP_TOLERANCE = 10000;
	private static final int THREAD_SHUTDOWN_TOLERANCE = 10000;

	public abstract AlgorithmProblemTransformer<P, I> getProblemReducer();

	public abstract IAlgorithmFactory<I, O> getFactory();

	public abstract I getSimpleProblemInputForGeneralTestPurposes() throws Exception;

	public abstract I getDifficultProblemInputForGeneralTestPurposes() throws Exception; // runtime at least 10 seconds

	@Test
	public void testStartAndFinishEventEmissionSequentially() throws Exception {
		int numberOfThreadsBefore = Thread.activeCount();
		IAlgorithmFactory<I, O> factory = getFactory();
		factory.setProblemInput(getSimpleProblemInputForGeneralTestPurposes());
		IAlgorithm<I, O> algorithm = factory.getAlgorithm();
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		CheckingEventListener listener = new CheckingEventListener();
		algorithm.registerListener(listener);
		algorithm.call();
		listener.checkState();
		waitForThreadsToAssumeNumber(numberOfThreadsBefore);
		checkNotInterrupted();
	}

	@Test
	public void testStartAndFinishEventEmissionProtocolParallelly() throws Exception {
		IAlgorithmFactory<I, O> factory = getFactory();
		factory.setProblemInput(getSimpleProblemInputForGeneralTestPurposes());
		IAlgorithm<I, O> algorithm = factory.getAlgorithm();
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
		CheckingEventListener listener = new CheckingEventListener();
		algorithm.registerListener(listener);
		int numberOfThreadsBefore = Thread.activeCount();
		algorithm.call();
		listener.checkState();
		waitForThreadsToAssumeNumber(numberOfThreadsBefore);
		checkNotInterrupted();
	}

	@Test
	public void testStartAndFinishEventEmissionByIteration() throws Exception {
		IAlgorithmFactory<I, O> factory = getFactory();
		factory.setProblemInput(getSimpleProblemInputForGeneralTestPurposes());
		IAlgorithm<I, O> algorithm = factory.getAlgorithm();
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		CheckingEventListener listener = new CheckingEventListener();
		int numberOfThreadsBefore = Thread.activeCount();
		for (AlgorithmEvent e : algorithm) {
			listener.receiveEvent(e);
		}
		listener.checkState();
		waitForThreadsToAssumeNumber(numberOfThreadsBefore);
		checkNotInterrupted();
	}

	@Test
	public void testInterrupt() throws Exception {

		/* set up algorithm */
		IAlgorithmFactory<I, O> factory = getFactory();
		factory.setProblemInput(getDifficultProblemInputForGeneralTestPurposes());
		IAlgorithm<I, O> algorithm = factory.getAlgorithm();
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
		algorithm.setMaxNumThreads(Runtime.getRuntime().availableProcessors());
		FutureTask<O> task = new FutureTask<>(algorithm);
		int numberOfThreadsBefore = Thread.activeCount();

		/* set up timer for interruption */
		Thread t = new Thread(task, "InterruptTest Algorithm runner for " + algorithm.getId());
		AtomicLong interruptEvent = new AtomicLong();
		t.start();
		new Timer("InterruptTest Timer").schedule(new TimerTask() {
			@Override
			public void run() {
				logger.info("Interrupting thread {}", t);
				t.interrupt();
				interruptEvent.set(System.currentTimeMillis());
			}
		}, INTERRUPTION_DELAY);
		
		/* launch algorithm */
		boolean interruptedExceptionSeen = false;
		boolean timeoutTriggered = false;
		long start = System.currentTimeMillis();
		try {
			O output = task.get(INTERRUPTION_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS);
			assert false : ("Algorithm terminated without exception but with regular output: " + output);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof InterruptedException)
				interruptedExceptionSeen = true;
			else {
				throw e;
			}
		} catch (TimeoutException e) {
			timeoutTriggered = true;
		}
		int runtime = (int) (System.currentTimeMillis() - start);
		logger.info("Executing thread has returned control after {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime);
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY);
		assertFalse("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after the interrupt.", timeoutTriggered);
		assertTrue("The algorithm has not emitted an interrupted exception.", interruptedExceptionSeen);

		/*
		 * now sending a cancel to make sure the algorithm structure is shutdown (this
		 * is because the interrupt only requires that the executing thread is returned
		 * but not that the algorithm is shutdown
		 */
		algorithm.cancel();
		waitForThreadsToAssumeNumber(numberOfThreadsBefore);
		logger.info("Interrupt-Test finished.");
	}

	@Test
	public void testCancel() throws Exception {

		/* set up algorithm */
		IAlgorithmFactory<I, O> factory = getFactory();
		factory.setProblemInput(getDifficultProblemInputForGeneralTestPurposes());
		IAlgorithm<I, O> algorithm = factory.getAlgorithm();
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		int availableCPUs = Runtime.getRuntime().availableProcessors();
		algorithm.setNumCPUs(availableCPUs);
		algorithm.setMaxNumThreads(availableCPUs);
		FutureTask<O> task = new FutureTask<>(algorithm);
		int numberOfThreadsBefore = Thread.activeCount();

		/* set up timer for interruption */
		AtomicLong cancelEvent = new AtomicLong();
		new Timer("CancelTest Timer").schedule(new TimerTask() {
			@Override
			public void run() {
				logger.info("Triggering cancel");
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
		ThreadGroupObserver threadCountObserverThread = new ThreadGroupObserver(algorithmThreadGroup, algorithm.getConfig().threads(), () -> {threadNumberViolated.set(true); algorithm.cancel(); });
		threadCountObserverThread.start();
		
		/* launch algorithm */
		algorithmThread.start();
		try {
			O output = task.get(INTERRUPTION_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS);
			assert false : ("Algorithm terminated without exception but with regular output: " + output);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof AlgorithmExecutionCanceledException) {
				AlgorithmExecutionCanceledException ex = (AlgorithmExecutionCanceledException)e.getCause();
				cancellationExceptionSeen = true;
				if (ex.getDelay() > 500) {
					logger.error("The algorithm has sent an AlgorithmExceutionCanceledException, which is correct, but the cancel was triggered with a delay of {}ms, which exceeds the allowed time of 500ms.", ex.getDelay());
					throw e;
				}
			}
			
			/* if the max number of threads has been violated, reset interrupted flag */
			if (threadNumberViolated.get()) {
				Thread.interrupted(); // this was a controlled interrupt, reset the flag
			}
			else {
				throw e;
			}
		} catch (TimeoutException e) {
			timeoutTriggered = true;
		}
		int runtime = (int) (System.currentTimeMillis() - start);
		assertFalse("Thread must not be interrupted after cancel!", Thread.currentThread().isInterrupted());
		threadCountObserverThread.cancel();
		logger.info("Executing thread has returned control after {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime);
		assertTrue("The number of threads used during execution reached " + threadCountObserverThread.getMaxObservedThreads() + " while allowed maximum is " + availableCPUs + ". Observed threads: \n\t- " + Arrays.asList(threadCountObserverThread.getThreadsAtPointOfViolation() != null ? threadCountObserverThread.getThreadsAtPointOfViolation() : new Thread[0]).stream().map(Thread::getName).collect(Collectors.joining("\n\t- ")), !threadCountObserverThread.isThreadConstraintViolated());
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY);
		assertFalse("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after it has been canceled.", timeoutTriggered);
		assertTrue("The algorithm has not emitted an AlgorithmExecutionCanceledException.", cancellationExceptionSeen);
		waitForThreadsToAssumeNumber(numberOfThreadsBefore);
		checkNotInterrupted();
		logger.info("Cancel-Test finished.");
	}

	@Test
	public void testQuickTimeout() throws Exception {

		/* set up algorithm */
		IAlgorithmFactory<I, O> factory = getFactory();
		factory.setProblemInput(getDifficultProblemInputForGeneralTestPurposes());
		IAlgorithm<I, O> algorithm = factory.getAlgorithm();
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		int availableCPUs = Runtime.getRuntime().availableProcessors();
		algorithm.setNumCPUs(availableCPUs);
		algorithm.setMaxNumThreads(availableCPUs);
		assert algorithm.getConfig().threads() == availableCPUs;
		FutureTask<O> task = new FutureTask<>(algorithm);
		algorithm.setTimeout(INTERRUPTION_DELAY, TimeUnit.MILLISECONDS);
		int numberOfThreadsBefore = Thread.activeCount();
		
		/* prepare algorithm thread with a new thread group so that the algorithm can be monitored more easily */
		long start = System.currentTimeMillis();
		boolean timeoutedExceptionSeen = false;
		boolean timeoutTriggered = false;
		ThreadGroup tg = new ThreadGroup("TimeoutTestGroup");
		Thread algorithmThread = new Thread(tg, task, "TimeoutTest Algorithm runner for " + algorithm.getId());
		AtomicBoolean threadNumberViolated = new AtomicBoolean();
		ThreadGroupObserver threadCountObserverThread = new ThreadGroupObserver(tg, algorithm.getConfig().threads(), () -> {threadNumberViolated.set(true); algorithm.cancel(); });
		threadCountObserverThread.start();
		
		/* launch algorithm */
		algorithmThread.start();
		try {
			O output = task.get(INTERRUPTION_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS);
			assert false : ("Algorithm terminated without exception but with regular output: " + output);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof AlgorithmTimeoutedException) {
				timeoutedExceptionSeen = true;
				AlgorithmTimeoutedException ex = (AlgorithmTimeoutedException)e.getCause();
				if (ex.getDelay() > 500) {
					logger.error("The algorithm has sent a TimeoutException, which is correct, but the timeout was triggered with a delay of {}ms, which exceeds the allowed time of 500ms.", ex.getDelay());
					throw e;
				}
			}
			else if (e.getCause() instanceof AlgorithmExecutionCanceledException && threadNumberViolated.get()) {
				Thread.interrupted(); // this was a controlled interrupt, reset the flag
			}
			else {
				throw e;
			}
		} catch (TimeoutException e) {
			timeoutTriggered = true;
		}
		long end = System.currentTimeMillis();
		int runtime = (int) (end - start);
		assertFalse("Thread must not be interrupted after timeout!", Thread.currentThread().isInterrupted());
		threadCountObserverThread.cancel();
		assertTrue("The number of threads used during execution reached " + threadCountObserverThread.getMaxObservedThreads() + " while allowed maximum is " + availableCPUs + ". Observed threads: \n\t- " + Arrays.asList(threadCountObserverThread.getThreadsAtPointOfViolation() != null ? threadCountObserverThread.getThreadsAtPointOfViolation() : new Thread[0]).stream().map(Thread::getName).collect(Collectors.joining("\n\t- ")), !threadCountObserverThread.isThreadConstraintViolated());
		logger.info("Executing thread has returned control after {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime);
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY);
		assertFalse("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + " ms after the specified timeout.", timeoutTriggered);
		assertTrue("The algorithm has not emitted an TimeoutException.", timeoutedExceptionSeen);
		waitForThreadsToAssumeNumber(numberOfThreadsBefore);
		checkNotInterrupted();
		logger.info("Timeout-Test finished.");
	}

	private void checkNotInterrupted() {
		assertTrue("Executing thread is interrupted, which must not be the case!", !Thread.currentThread().isInterrupted());
	}

	private void waitForThreadsToAssumeNumber(int maximumNumberOfThreads) throws InterruptedException {
		logger.info("Waiting for number of threads to become {}.", maximumNumberOfThreads);
		int sleepTime = 100;
		int n = THREAD_SHUTDOWN_TOLERANCE / sleepTime;
		int numberOfThreadsAfter = Thread.activeCount();
		for (int i = 0; i < n && numberOfThreadsAfter > maximumNumberOfThreads; i++) {
			logger.info("Thread wait {}/{}: There are {} threads active compared to {} that were running prior to test. Waiting {}ms for another check.", i + 1, n, numberOfThreadsAfter, maximumNumberOfThreads, sleepTime);
			Thread.sleep(sleepTime);
			numberOfThreadsAfter = Thread.activeCount();
		}
		assertTrue("Number of threads has increased with execution", maximumNumberOfThreads >= numberOfThreadsAfter);
	}

	private class CheckingEventListener {
		boolean observedInit = false;
		boolean observedInitExactlyOnce = false;
		boolean observedInitBeforeFinish = false;
		boolean observedFinish = false;
		boolean observedFinishExactlyOnce = false;

		public void receiveEvent(AlgorithmEvent e) {

			if (e instanceof AlgorithmInitializedEvent)
				receiveEvent((AlgorithmInitializedEvent) e);
			else if (e instanceof AlgorithmFinishedEvent)
				receiveEvent((AlgorithmFinishedEvent) e);

			/* ignore other events */
		}

		@Subscribe
		public void receiveEvent(AlgorithmInitializedEvent e) {
			if (!observedInit) {
				observedInit = true;
				observedInitExactlyOnce = true;
				if (!observedFinish)
					observedInitBeforeFinish = true;
			} else {
				observedInitExactlyOnce = false;
			}
		}

		@Subscribe
		public void receiveEvent(AlgorithmFinishedEvent e) {
			if (!observedFinish) {
				observedFinish = true;
				observedFinishExactlyOnce = true;
			} else
				observedFinishExactlyOnce = false;
		}

		void checkState() {
			assertTrue("No init event was observed", observedInit);
			assertTrue("More than one init event was observed", observedInitExactlyOnce);
			assertTrue("A finish event was observed prior to an init event", observedInitBeforeFinish);
			assertTrue("No finish event was observed", observedFinish);
			assertTrue("More than one finish event was observed", observedFinishExactlyOnce);
		}
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}

	protected Logger getLogger() {
		return logger;
	}

	@Override
	public void setLoggerName(String name) {
		logger.info("Switching logger name from {} to {}.", loggerName, name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(loggerName);
		logger.info("Switched logger name to {}.", loggerName);
	}

}
