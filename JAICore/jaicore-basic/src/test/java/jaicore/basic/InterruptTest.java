package jaicore.basic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.awaitility.Awaitility;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.concurrent.GlobalTimer;
import jaicore.interrupt.Interrupt;
import jaicore.interrupt.Interrupter;
import jaicore.interrupt.InterruptionTimerTask;
import jaicore.timing.TimedComputation;

public class InterruptTest {

	private static final int NUMBER_ITERATIONS_SIMPLE = 5;
	private static final int NUMBER_ITERATIONS_SHIFTED = 15;
	private static final int NUMBER_ITERATIONS_OVERLAPPING = 100;
	
	private static final Logger logger = LoggerFactory.getLogger(InterruptTest.class);

	private class BusyBeaver implements Callable<Object> {

		private long goal;

		public BusyBeaver(final long goal) {
			super();
			this.goal = goal;
		}

		@Override
		public Object call() throws Exception {
			long i = 0;
			while (i < goal) {
				i++;
				if (i % 1000 == 0 && Thread.interrupted()) {
					throw new InterruptedException();
				}
			}
			return null;
		}
	}

	@Test
	public void testSimpleInterruptDuringExecution() throws ExecutionException, InterruptedException {
		for (int i = 0; i < NUMBER_ITERATIONS_SIMPLE; i++) {

			/* test that InterruptException is thrown  */
			try {
				TimedComputation.compute(new BusyBeaver(Long.MAX_VALUE), 1000, "bb interrupt");
			} catch (AlgorithmTimeoutedException e) {

				/* this is expected behavior */
				assertTrue(GlobalTimer.getInstance().getActiveTasks().isEmpty());
				continue;
			}
			fail("No exception was thrown, but an AlgorithmTimedoutedException should have been thrown!");
		}
	}

	@Test
	public void testThatNoInterruptIsFiredIfExecutionFinishesInTime() throws AlgorithmTimeoutedException, ExecutionException, InterruptedException {
		for (int i = 0; i < NUMBER_ITERATIONS_SIMPLE; i++) {

			/* test that InterruptException is thrown  */
			TimedComputation.compute(new BusyBeaver(100), 1000, "bb interrupt");
			assertTrue(GlobalTimer.getInstance().getActiveTasks().isEmpty());
			Awaitility.await().atLeast(2, TimeUnit.SECONDS);
			assertTrue(!Thread.interrupted());
		}
	}

	/**
	 * Two timed computations are nested. The outer is canceled significantly (1s) earlier than the inner.
	 *
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@Test
	public void testNestedInterruptDuringExecutionWithOuterSignifiantlyEarlier() throws ExecutionException, InterruptedException {

		for (int i = 0; i < NUMBER_ITERATIONS_SHIFTED; i++) {

			/* test that InterruptException is thrown and that no interrupts are open */
			try {
				TimedComputation.compute(() -> TimedComputation.compute(new BusyBeaver(Long.MAX_VALUE), 2000, "inner interrupt"), 1000, "outer interrupt");
			} catch (AlgorithmTimeoutedException e) {

				/* this is expected behavior */
				assertTrue(GlobalTimer.getInstance().getActiveTasks().isEmpty());
				assertFalse(Interrupter.get().hasCurrentThreadOpenInterrupts());
				Awaitility.await().atLeast(2, TimeUnit.SECONDS);
				assertTrue(GlobalTimer.getInstance().getActiveTasks().isEmpty());
				assertFalse(Interrupter.get().hasCurrentThreadOpenInterrupts());
				continue;
			}
			fail("No exception was thrown, but an AlgorithmTimedoutedException should have been thrown!");
		}
	}

	/**
	 * Two timed computations are nested. The outer is canceled significantly (1s) later than the inner.
	 *
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws AlgorithmTimeoutedException
	 */
	@Test
	public void testNestedInterruptDuringExecutionWithOuterSignifiantlyLater() throws InterruptedException, AlgorithmTimeoutedException, ExecutionException {
		for (int i = 0; i < NUMBER_ITERATIONS_SHIFTED; i++) {

			/* test that InterruptException is thrown and that no interrupts are open */
			try {
				TimedComputation.compute(() -> TimedComputation.compute(new BusyBeaver(Long.MAX_VALUE), 1000, "inner interrupt"), 2000, "outer interrupt");
			} catch (ExecutionException e) {

				if (!(e.getCause() instanceof AlgorithmTimeoutedException)) {
					throw e;
				}

				/* this is expected behavior */
				assertTrue(GlobalTimer.getInstance().getActiveTasks().isEmpty());
				assertFalse(Interrupter.get().hasCurrentThreadOpenInterrupts());
				Awaitility.await().atLeast(2, TimeUnit.SECONDS);
				assertTrue(GlobalTimer.getInstance().getActiveTasks().isEmpty());
				assertFalse(Interrupter.get().hasCurrentThreadOpenInterrupts());
				continue;
			}
			fail("No exception was thrown, but an AlgorithmTimedoutedException should have been thrown!");
		}
	}

	/**
	 * Two timed computations are nested. The outer is canceled significantly (1s) later than the inner.
	 * @throws  
	 *
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws AlgorithmTimeoutedException
	 */
	@Test
	public void testNestedInterruptDuringExecutionWithOuterAndInnerAtSameTime() throws ExecutionException {

		for (int i = 0; i < NUMBER_ITERATIONS_OVERLAPPING; i++) {
			logger.info("Starting iteration {}/{} of testNestedInterruptDuringExecutionWithOuterAndInnerAtSameTime", i + 1, NUMBER_ITERATIONS_OVERLAPPING);

			/* test that InterruptException is thrown and that no interrupts are open */
			Exception caughtException = null;
			try {
				TimedComputation.compute(() -> TimedComputation.compute(new BusyBeaver(Long.MAX_VALUE), 1000, "inner interrupt"), 1000, "outer interrupt");
			} catch (Exception e) {
				caughtException = e;
			}
			
			/* check exception */
			if (caughtException instanceof ExecutionException && !(caughtException.getCause() instanceof AlgorithmTimeoutedException)) {
				throw (ExecutionException)caughtException;
			}

			if (caughtException != null) {
				/* this is expected behavior */
				assertFalse("The executing thread is interrupted after a computation block!", Thread.interrupted());
				assertTrue(GlobalTimer.getInstance().getActiveTasks().isEmpty());
				assertFalse("There are open interrupts: " + Interrupter.get().getAllUnresolvedInterruptsOfThread(Thread.currentThread()).stream().map(Interrupt::getReasonForInterruption).collect(Collectors.toList()),
						Interrupter.get().hasCurrentThreadOpenInterrupts());
			}
			else {
				fail("No exception was thrown, but an AlgorithmTimedoutedException should have been thrown!");
			}
		}
	}

	@Test
	public void testTwistedInterruptDuringExecutionWithOuterSignifiantlyEarlier() throws InterruptedException, AlgorithmTimeoutedException, ExecutionException {
		for (int i = 0; i < NUMBER_ITERATIONS_SHIFTED; i++) {

			/* test that InterruptException is thrown and that no interrupts are open */
			InterruptionTimerTask task = new InterruptionTimerTask("outer interrupt");
			GlobalTimer.getInstance().schedule(task, 1000);
			try {
				TimedComputation.compute(new BusyBeaver(Long.MAX_VALUE), 1500, "inner interrupt");
				fail("No exception was thrown, but an AlgorithmTimedoutedException should have been thrown!");
			}
			catch (InterruptedException e) {

				/* this is expected behavior */
				while (!task.isFinished()) {
					Awaitility.await().atLeast(100, TimeUnit.MILLISECONDS);
				}
				
				boolean interrupted = Thread.interrupted();
				logger.debug("Now resolving the interrupt. Current interrupted flag state: {}", interrupted);
				Interrupter.get().markInterruptOnCurrentThreadAsResolved(task);
				assertTrue(GlobalTimer.getInstance().getActiveTasks().isEmpty());
				assertFalse(Interrupter.get().hasCurrentThreadOpenInterrupts());
				
				/* this is just to make sonarqube happy */
				Thread.currentThread().interrupt();
				if (!interrupted)
					Thread.interrupted();
			}
		}
	}

	@Test
	public void testTwistedInterruptDuringExecutionWithOuterSignifiantlyLater() throws InterruptedException, AlgorithmTimeoutedException, ExecutionException {
		for (int i = 0; i < NUMBER_ITERATIONS_SHIFTED; i++) {

			/* test that InterruptException is thrown and that no interrupts are open */
			InterruptionTimerTask task = new InterruptionTimerTask("outer interrupt");
			GlobalTimer.getInstance().schedule(task, 1500);
			try {
				TimedComputation.compute(new BusyBeaver(Long.MAX_VALUE), 1000, "inner interrupt");
			}
			catch (AlgorithmTimeoutedException e) {

				/* check that thread is not interrupted */
				assertFalse(Thread.interrupted());

				/* this is expected behavior */
				while (!task.isFinished()) {
					Awaitility.await().atLeast(100, TimeUnit.MILLISECONDS);
				}
				boolean interrupted = Thread.interrupted();
				logger.debug("Now resolving the interrupt. Current interrupted flag state: {}", interrupted);
				Interrupter.get().markInterruptOnCurrentThreadAsResolved(task);
				assertTrue(GlobalTimer.getInstance().getActiveTasks().isEmpty());
				assertFalse(Interrupter.get().hasCurrentThreadOpenInterrupts());
				continue;
			}
			fail("No exception was thrown, but an AlgorithmTimedoutedException should have been thrown!");
		}
	}

	@Test
	public void testTwistedTrackedInterruptDuringExecutionWithOuterAndInnerAtSameTime() throws InterruptedException, AlgorithmTimeoutedException, ExecutionException {
		int innerEarlier = 0;
		int outerEarlier = 0;
		for (int i = 0; i < NUMBER_ITERATIONS_OVERLAPPING; i++) {
			logger.info("Starting iteration {}/{} of testTwistedTrackedInterruptDuringExecutionWithOuterAndInnerAtSameTime", i + 1, NUMBER_ITERATIONS_OVERLAPPING);
			assertTrue("There are still active tasks!", GlobalTimer.getInstance().getActiveTasks().isEmpty());

			/* test that InterruptException is thrown and that no interrupts are open */
			InterruptionTimerTask task = new InterruptionTimerTask("outer interrupt");
			int timeout = 1000;
			GlobalTimer.getInstance().schedule(task, timeout + Math.round(Math.random())); // increase timeout by 1 ms in the first 10 runs to balance scheduling disadvanates of the outer one
			try {
				TimedComputation.compute(new BusyBeaver(Long.MAX_VALUE), timeout, "inner interrupt");
				fail("Operation should not stop without exception!");
			} catch (InterruptedException e) {
				outerEarlier++;
				Interrupter.get().markInterruptAsResolved(Thread.currentThread(), task);
				assertTrue("There has been an interrupted exception, but the task has not fired! Stack trace: " + Arrays.stream(e.getStackTrace()).map(s -> "\n\t" + s).collect(Collectors.toList()), task.isTriggered());
				assertTrue(task.isFinished());
				
				/* this is just to make sonarqube happy */
				boolean interrupted = Thread.interrupted();
				Thread.currentThread().interrupt();
				if (!interrupted)
					Thread.interrupted();
			} catch (AlgorithmTimeoutedException e) {
				innerEarlier++;

				/* wait for outer interrupt to occur */
				while (!task.isFinished()) {
					Awaitility.await().atLeast(100, TimeUnit.MILLISECONDS);
					if (Thread.interrupted()) {
						logger.debug("Interrupt received.");
					}
				}
				boolean interrupted = Thread.interrupted();
				logger.debug("Now resolving the interrupt. Current interrupted flag state: {}", interrupted);
				Interrupter.get().markInterruptOnCurrentThreadAsResolved(task);
			}

			assertTrue(!Thread.currentThread().isInterrupted());
			assertTrue(task.isFinished());
			assertTrue("Interrupter has still unresolved interrupts: " + Interrupter.get().getAllUnresolvedInterrupts().stream().map(Interrupt::getReasonForInterruption).collect(Collectors.toList()),
					Interrupter.get().getAllUnresolvedInterrupts().isEmpty());
			assertTrue("There are still active tasks!", GlobalTimer.getInstance().getActiveTasks().isEmpty());
			logger.debug("Finished. Inner earlier: {}. Outer earlier: {}", innerEarlier, outerEarlier);
		}
	}
}
