package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
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
public abstract class TimeAwareNodeEvaluator<T, V extends Comparable<V>> implements INodeEvaluator<T, V> {

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
		AtomicBoolean controlledInterrupt = new AtomicBoolean(false);
		TimeoutSubmitter ts = TimeoutTimer.getInstance().getSubmitter();
		TimerTask timerTask = ts.interruptMeAfterMS(interruptionTime, "Node evaluation has timed out (" + TimeAwareNodeEvaluator.class.getName() + ")", () -> controlledInterrupt.set(true));
		activeTimerTasks.add(timerTask);
		try {
			V result = this.fTimeouted(node, grantedTime);
			timerTask.cancel();
			activeTimerTasks.remove(timerTask);
			ts.close();
			return result;
		} catch (InterruptedException e) {
			timerTask.cancel();
			activeTimerTasks.remove(timerTask);
			Thread.interrupted(); // clear interrupted field
			if (controlledInterrupt.get()) {
				return this.fallbackNodeEvaluator.f(node);
			} else {
				throw e;
			}
		}
	}
	
	public void cancelActiveTasks() {
		while (!activeTimerTasks.isEmpty()) {
			TimerTask tt = activeTimerTasks.remove(0);
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
}
