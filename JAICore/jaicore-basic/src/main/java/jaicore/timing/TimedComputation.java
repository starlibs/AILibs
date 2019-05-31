package jaicore.timing;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.concurrent.GlobalTimer;
import jaicore.interrupt.Interrupter;
import jaicore.interrupt.InterruptionTimerTask;

/**
 * This class is the single-thread pendant to asynchronous computations realized with Futures
 *
 * @author fmohr
 *
 */
public abstract class TimedComputation {

	private static final Logger logger = LoggerFactory.getLogger(TimedComputation.class);

	private TimedComputation() {
		/* no explicit instantiation allowed */
	}

	public static <T> T compute(final Callable<T> callable, final long timeoutInMs, final String reasonToLogOnTimeout) throws ExecutionException, AlgorithmTimeoutedException, InterruptedException {

		/* schedule a timer that will interrupt the current thread and execute the task */
		GlobalTimer timer = GlobalTimer.getInstance();
		long start = System.currentTimeMillis();
		InterruptionTimerTask task = new InterruptionTimerTask("Timeout for timed computation with thread " + Thread.currentThread() + " at timestamp " + start + ": " + reasonToLogOnTimeout);
		logger.debug("Scheduling timer for interruption in {}ms, i.e. timestamp {}. List of active tasks is now: {}", timeoutInMs, start + timeoutInMs, timer.getActiveTasks());
		timer.schedule(task, timeoutInMs);
		Interrupter interrupter = Interrupter.get();
		T output = null;
		Exception caughtException = null;
		try {
			logger.debug("Starting supervised computation of {}.", callable);
			output = callable.call();
		} catch (Exception e) {
			caughtException = e;
		} finally {
			task.cancel();
		}

		/* several circumstances define the state at this point:
		 * 1. the Callable has exited successfully or not
		 * 2. the thread is currently not interrupted or interrupted internally or externally
		 * 3. the time elapsed since calling the Callable is lower than the timeout or exceeds it
		 *
		 */
		int runtime = (int) (System.currentTimeMillis() - start);
		int delay = runtime - (int) timeoutInMs;
		if (caughtException != null) {
			logger.info("Timed computation has returned control after {}ms, i.e., with a delay of {}ms. Observed exception: {}", runtime, delay, caughtException.getClass().getName());
		} else {
			logger.info("Timed computation has returned control after {}ms, i.e., with a delay of {}ms. Observed regular output return value: {}", runtime, delay, output);
		}

		/* now make sure that
		 * a) the timeoutTriggered flag is true iff the TimerTask for the timeout has been executed
		 * b) the interrupt-flag of the thread is true iff there has been another open (or untracked) interrupt
		 * */
		boolean timeoutTriggered = false;
		synchronized (interrupter) {

			/* if the timeout has been triggered (with caution) */
			logger.debug("Checking for an interruption and resolving potential interrupts.");
			if (interrupter.hasCurrentThreadBeenInterruptedWithReason(task)) {
				logger.info("Thread has been interrupted internally. Resolving the interrupt (this may throw an InterruptedException).");
				timeoutTriggered = true;
				Thread.interrupted(); // clear the interrupted field
				try {
					Interrupter.get().markInterruptOnCurrentThreadAsResolved(task);
				} catch (InterruptedException e) {
					logger.debug("Re-interrupting current thread, because another interrupt has been open.");
					Thread.currentThread().interrupt();
				}
			}

			/* otherwise, if the thread has been interrupted directly and not as a consequence of a shutdown, forward the interrupt */
			else if (task.isTriggered()) {
				interrupter.avoidInterrupt(Thread.currentThread(), task);
				logger.info("Interrupt is external, black-listed \"{}\" for interrupts on {} and re-throwing the exception.", task, Thread.currentThread());
			}
			assert !interrupter.hasCurrentThreadBeenInterruptedWithReason(task);
		}

		/* if there has been an exception, throw it if it is not the InterruptedException caused by the timeout (in this case, throw an AlgorithmTimeoutedException) */
		if (caughtException != null) {
			if (timeoutTriggered) {
				logger.info("Throwing TimeoutException");
				throw new AlgorithmTimeoutedException(delay);
			} else if (caughtException instanceof InterruptedException) {
				logger.debug("Now re-throwing {}, which was caught in timed computation. Interrupt-flag is {}.", caughtException, Thread.currentThread().isInterrupted());
				throw (InterruptedException) caughtException;
			} else {
				logger.debug("Now re-throwing {}, which was caught in timed computation. Interrupt-flag is {}.", caughtException, Thread.currentThread().isInterrupted());
				throw new ExecutionException(caughtException);
			}
		}

		/* if no exception has been thrown, return the received output. Maybe the thread is interrupted, but this is then not due to our timeout */
		logger.debug("Finished timed computation of {} after {}ms where {}ms were allowed. Interrupt-flag is {}", callable, runtime, timeoutInMs, Thread.currentThread().isInterrupted());
		return output;
	}
}
