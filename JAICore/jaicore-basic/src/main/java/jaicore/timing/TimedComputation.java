package jaicore.timing;

import java.util.TimerTask;
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

	public static <T> T compute(Callable<T> callable, long timeoutInMs, final String reasonToLogOnTimeout) throws ExecutionException, AlgorithmTimeoutedException, InterruptedException {

		/* schedule a timer that will interrupt the current thread and execute the task */
		GlobalTimer timer = GlobalTimer.getInstance();
		long start = System.currentTimeMillis();
		TimerTask task = new InterruptionTimerTask("Timeout for timed computation with thread " + Thread.currentThread() + " at timestamp " + start + ": " + reasonToLogOnTimeout);
		logger.debug("Scheduling timer for interruption in {}ms, i.e. timestamp {}. List of active tasks is now: {}", timeoutInMs, start + timeoutInMs, timer.getActiveTasks());
		timer.schedule(task, timeoutInMs);
		try {
			logger.debug("Starting supervised computation of {}.", callable);
			return callable.call();
		} catch (InterruptedException e) { // the fact that we are interrupted here can have several reasons. Could be an interrupt from the outside, a cancel, or a timeout by the above timer
			logger.info("Received interrupt in timed computation of {}", callable);
			Interrupter interrupter = Interrupter.get();
			synchronized (interrupter) {

				/* if the timeout has been triggered (with caution) */
				if (interrupter.hasCurrentThreadBeenInterruptedWithReason(task)) {
					logger.info("Interrupt is internal. Resolving interrupt.");
					Thread.interrupted(); // clear the interrupted field
					Interrupter.get().markInterruptOnCurrentThreadAsResolved(task);
					assert !interrupter.hasCurrentThreadBeenInterruptedWithReason(task);
					logger.info("Throwing TimeoutException");
					throw new AlgorithmTimeoutedException(timeoutInMs - (System.currentTimeMillis() - start));
				}

				/* otherwise, if the thread has been interrupted directly and not as a consequence of a shutdown, forward the interrupt */
				else {
					logger.info("Interrupt is external, black-listing this interrupt and re-throwing the exception.");
					interrupter.avoidInterrupt(Thread.currentThread(), reasonToLogOnTimeout);
					throw e;
				}
			}
		} catch (Exception e) {
			throw new ExecutionException(e);
		} finally {
			task.cancel();
			logger.debug("Finished timed computation of {} after {}ms where {}ms were allowed. Task {} is canceled.", callable, System.currentTimeMillis() - start, timeoutInMs, task);
		}
	}
}
