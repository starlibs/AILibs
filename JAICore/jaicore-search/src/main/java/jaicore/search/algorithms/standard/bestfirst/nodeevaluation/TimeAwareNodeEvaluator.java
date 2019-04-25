package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.concurrent.GlobalTimer;
import jaicore.concurrent.GlobalTimer.TimeoutSubmitter;
import jaicore.interrupt.Interrupter;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.model.travesaltree.Node;

/**
 * This class can be used to create node evaluators with a time limit for the evaluation of each node.
 * The remaining time for the evaluation of the node is given with the call of f.
 *
 * The thread will be interrupted after the timeout if it has not returned control in time.
 *
 * @author fmohr
 *
 * @param <T>
 * @param <V>
 */
public abstract class TimeAwareNodeEvaluator<T, V extends Comparable<V>> implements INodeEvaluator<T, V>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(TimeAwareNodeEvaluator.class);
	private final int timeoutForNodeEvaluationInMS;
	private long totalDeadline = -1; // this deadline can be set to guarantee that there will be no activity after this timestamp
	private final INodeEvaluator<T, V> fallbackNodeEvaluator;
	private final List<TimerTask> activeTimerTasks = new ArrayList<>();

	public TimeAwareNodeEvaluator(final int pTimeoutInMS) {
		this(pTimeoutInMS, n -> null);
	}

	public TimeAwareNodeEvaluator(final int pTimeoutInMS, final INodeEvaluator<T, V> pFallbackNodeEvaluator) {
		super();
		this.timeoutForNodeEvaluationInMS = pTimeoutInMS;
		this.fallbackNodeEvaluator = pFallbackNodeEvaluator;
	}

	protected abstract V fTimeouted(Node<T, ?> node, int timeoutInMS) throws NodeEvaluationException, InterruptedException;

	@Override
	public final V f(final Node<T, ?> node) throws NodeEvaluationException, InterruptedException {

		/* determine time available and granted for node evaluation */
		int remainingTime;
		if (this.totalDeadline >= 0 && this.timeoutForNodeEvaluationInMS >= 0) {
			remainingTime = Math.min(this.timeoutForNodeEvaluationInMS, (int) (this.totalDeadline - System.currentTimeMillis()));
		} else if (this.totalDeadline >= 0) {
			remainingTime = (int) (this.totalDeadline - System.currentTimeMillis());
		} else if (this.timeoutForNodeEvaluationInMS >= 0) {
			remainingTime = this.timeoutForNodeEvaluationInMS;
		} else {
			remainingTime = Integer.MAX_VALUE - 1000;
		}
		int grantedTime = remainingTime - 50;
		int interruptionTime = remainingTime + 150;

		/* execute evaluation */
		TimeoutSubmitter ts = GlobalTimer.getInstance().getSubmitter();
		String reason = "Node evaluation has timed out (" + TimeAwareNodeEvaluator.class.getName() + "::" + Thread.currentThread() + "-" + System.currentTimeMillis() + ")";
		TimerTask timerTask = ts.interruptMeAfterMS(interruptionTime, reason);
		Interrupter interrupter = Interrupter.get();
		this.activeTimerTasks.add(timerTask);
		try {
			V result = this.fTimeouted(node, grantedTime);
			ts.close();
			return result;
		} catch (InterruptedException e) {
			synchronized (interrupter) {
				logger.info("Caught InterruptedException with message {}. Checking whether the scheduled task {} has been among the reasons.", e.getMessage(), reason);
				assert !Thread.currentThread().isInterrupted() : "Thread should not be interrupted when InterruptedException is thrown.";
				Thread.interrupted(); // clear interrupted field, just to be sure
				if (interrupter.hasCurrentThreadBeenInterruptedWithReason(reason)) {
					logger.debug("This is a controlled interrupt. Resolving the interrupt as marked and using the fallback (unless another interrupt exists)");
					interrupter.markInterruptOnCurrentThreadAsResolved(reason);
					return this.fallbackNodeEvaluator.f(node);
				} else {
					logger.debug("This is an uncontrolled (external) interrupt. Black-Listing my own reason {}.", reason);
					interrupter.avoidInterrupt(Thread.currentThread(), reason);
					throw e;
				}
			}
		} finally {
			this.activeTimerTasks.remove(timerTask);
			timerTask.cancel();
		}
	}

	public void cancelActiveTasks() {
		while (!this.activeTimerTasks.isEmpty()) {
			TimerTask tt = this.activeTimerTasks.remove(0);
			tt.cancel();
		}
	}

	public int getTimeoutForNodeEvaluationInMS() {
		return this.timeoutForNodeEvaluationInMS;
	}

	public INodeEvaluator<T, V> getFallbackNodeEvaluator() {
		return this.fallbackNodeEvaluator;
	}

	public long getTotalDeadline() {
		return this.totalDeadline;
	}

	public void setTotalDeadline(final long totalDeadline) {
		this.totalDeadline = totalDeadline;
	}

	protected void checkInterruption() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			Thread.interrupted(); // reset flag
			throw new InterruptedException("Node evaluation of " + this.getClass().getName() + " has been interrupted.");
		}
	}

	public void setLoggerName(String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched logger name to {}", name);
	}

	public String getLoggerName() {
		return this.logger.getName();
	}
}
