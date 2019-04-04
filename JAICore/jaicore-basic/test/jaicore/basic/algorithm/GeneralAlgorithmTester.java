package jaicore.basic.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.google.common.eventbus.Subscribe;

/**
 *
 * @param <P>
 *            The class of the actual problem to be solved
 * @param <I>
 *            The class of the algorithm input
 * @param <O>
 *            The class of the algorithm output
 */
public abstract class GeneralAlgorithmTester<P, I, O> {

	private static final int INTERRUPTION_DELAY = 5000;
	private static final int INTERRUPTION_CLEANUP_TOLERANCE = 2000;
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
				System.out.println("Interrupting " + t);
				t.interrupt();
				interruptEvent.set(System.currentTimeMillis());
			}
		}, INTERRUPTION_DELAY);

		/* launch algorithm */
		boolean interruptedExceptionSeen = false;
		long start = System.currentTimeMillis();
		try {
			task.get(INTERRUPTION_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof InterruptedException)
				interruptedExceptionSeen = true;
		} catch (TimeoutException e) {
		}
		long end = System.currentTimeMillis();
		int runtime = (int) (end - start);
		int timeNeededToRealizeInterrupt = (int) (end - interruptEvent.get());
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY);
		assertTrue("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after the interrupt.", timeNeededToRealizeInterrupt <= INTERRUPTION_CLEANUP_TOLERANCE);
		assertTrue("The algorithm has not emitted an interrupted exception.", interruptedExceptionSeen);

		/* now sending a cancel to make sure the algorithm structure is shutdown (this is because the interrupt only requires that the executing thread is returned but not that the algorithm is shutdown */
		algorithm.cancel();
		waitForThreadsToAssumeNumber(numberOfThreadsBefore);
	}

	@Test
	public void testCancel() throws Exception {

		/* set up algorithm */
		IAlgorithmFactory<I, O> factory = getFactory();
		factory.setProblemInput(getDifficultProblemInputForGeneralTestPurposes());
		IAlgorithm<I, O> algorithm = factory.getAlgorithm();
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
		Thread t = new Thread(task, "CancelTest Algorithm runner for " + algorithm);
		t.start();
		try {
			task.get(INTERRUPTION_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof AlgorithmExecutionCanceledException) {
				cancellationExceptionSeen = true;
			}
		} catch (TimeoutException e) {
		}
		long end = System.currentTimeMillis();
		int runtime = (int) (end - start);
		int timeNeededToRealizeCancel = (int) (end - cancelEvent.get());
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY);
		assertTrue("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + "ms after it has been canceled.", timeNeededToRealizeCancel < INTERRUPTION_CLEANUP_TOLERANCE);
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
		algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
		FutureTask<O> task = new FutureTask<>(algorithm);
		algorithm.setTimeout(INTERRUPTION_DELAY, TimeUnit.MILLISECONDS);
		int numberOfThreadsBefore = Thread.activeCount();

		/* launch algorithm */
		long start = System.currentTimeMillis();
		Thread t = new Thread(task, "TimeoutTest Algorithm runner for " + algorithm);
		t.start();
		try {
			task.get(INTERRUPTION_DELAY + INTERRUPTION_CLEANUP_TOLERANCE, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
		}
		long end = System.currentTimeMillis();
		int runtime = (int) (end - start);
		assertTrue("Runtime must be at least 5 seconds, actually should be at least 10 seconds.", runtime >= INTERRUPTION_DELAY);
		assertTrue("The algorithm has not terminated within " + INTERRUPTION_CLEANUP_TOLERANCE + " ms after the specified timeout.", runtime < INTERRUPTION_DELAY + INTERRUPTION_CLEANUP_TOLERANCE);
		waitForThreadsToAssumeNumber(numberOfThreadsBefore);
		checkNotInterrupted();
	}

	private void checkNotInterrupted() {
		assertTrue("Executing thread is interrupted, which must not be the case!", !Thread.currentThread().isInterrupted());
	}

	private void waitForThreadsToAssumeNumber(int numberOfThreads) throws InterruptedException {
		int n = 10;
		int numberOfThreadsAfter = Thread.activeCount();
		for (int i = 0; i < n && numberOfThreadsAfter > numberOfThreads; i++) {
			Thread.sleep(THREAD_SHUTDOWN_TOLERANCE / n);
			numberOfThreadsAfter = Thread.activeCount();
		}
		assertEquals("Number of threads has changed during execution", numberOfThreads, numberOfThreadsAfter);
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
}
