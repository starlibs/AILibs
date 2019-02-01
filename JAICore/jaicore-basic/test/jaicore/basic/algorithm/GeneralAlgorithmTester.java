package jaicore.basic.algorithm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;

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
		FutureTask<O> task = new FutureTask<>(algorithm);
		int numberOfThreadsBefore = Thread.activeCount();

		/* set up timer for interruption */
		Thread t = new Thread(task, "InterruptTest Algorithm runner for " + algorithm);
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
		algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
		FutureTask<O> task = new FutureTask<>(algorithm);
		int numberOfThreadsBefore = Thread.activeCount();

		/* set up timer for interruption */
		AtomicLong cancelEvent = new AtomicLong();
		new Timer("CancelTest Timer").schedule(new TimerTask() {
			@Override
			public void run() {
				algorithm.cancel();
				cancelEvent.set(System.currentTimeMillis());
			}
		}, INTERRUPTION_DELAY);

		/* launch algorithm */
		long start = System.currentTimeMillis();
		boolean cancellationExceptionSeen = false;
		boolean timeoutTriggered = false;
		Thread t = new Thread(task, "CancelTest Algorithm runner for " + algorithm);
		t.start();
		try {
			O output = task.get(INTERRUPTION_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS);
			assert false : ("Algorithm terminated without exception but with regular output: " + output);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof AlgorithmExecutionCanceledException) {
				cancellationExceptionSeen = true;
			}
			else {
				throw e;
			}
		} catch (TimeoutException e) {
			timeoutTriggered = true;
		}
		long end = System.currentTimeMillis();
		int runtime = (int) (end - start);
		logger.info("Executing thread has returned control after {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime);
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY);
		assertFalse("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after it has been canceled.", timeoutTriggered);
		assertTrue("The algorithm has not emitted an AlgorithmExecutionCanceledException.", cancellationExceptionSeen);
		waitForThreadsToAssumeNumber(numberOfThreadsBefore);
		checkNotInterrupted();
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
		algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
		FutureTask<O> task = new FutureTask<>(algorithm);
		algorithm.setTimeout(INTERRUPTION_DELAY, TimeUnit.MILLISECONDS);
		int numberOfThreadsBefore = Thread.activeCount();

		/* launch algorithm */
		long start = System.currentTimeMillis();
		boolean timeoutedExceptionSeen = false;
		boolean timeoutTriggered = false;
		Thread t = new Thread(task, "TimeoutTest Algorithm runner for " + algorithm);
		t.start();
		try {
			O output = task.get(INTERRUPTION_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS);
			assert false : ("Algorithm terminated without exception but with regular output: " + output);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof TimeoutException) {
				timeoutedExceptionSeen = true;
			}
			else {
				throw e;
			}
		} catch (TimeoutException e) {
			timeoutTriggered = true;
		}
		long end = System.currentTimeMillis();
		int runtime = (int) (end - start);
		logger.info("Executing thread has returned control after {}ms. Now observing metrics and waiting for possibly active sub-threads to shutdown.", runtime);
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY);
		assertFalse("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + " ms after the specified timeout.", timeoutTriggered);
		assertTrue("The algorithm has not emitted an TimeoutException.", timeoutedExceptionSeen);
		waitForThreadsToAssumeNumber(numberOfThreadsBefore);
		checkNotInterrupted();
	}

	private void checkNotInterrupted() {
		assertTrue("Executing thread is interrupted, which must not be the case!", !Thread.currentThread().isInterrupted());
	}

	private void waitForThreadsToAssumeNumber(int maximumNumberOfThreads) throws InterruptedException {
		int n = 10;
		int sleepTime = THREAD_SHUTDOWN_TOLERANCE / n;
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

	@Override
	public void setLoggerName(String name) {
		logger.info("Switching logger name from {} to {}.", loggerName, name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(loggerName);
		logger.info("Switched logger name to {}.", loggerName);
	}
	
	
}
