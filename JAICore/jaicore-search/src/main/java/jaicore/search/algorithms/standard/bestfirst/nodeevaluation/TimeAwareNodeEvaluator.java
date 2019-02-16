package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

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

	public TimeAwareNodeEvaluator(final int pTimeoutInMS) {
		this (pTimeoutInMS, n -> null);
	}
	
	public TimeAwareNodeEvaluator(final int pTimeoutInMS, final INodeEvaluator<T, V> pFallbackNodeEvaluator) {
		super();
		this.timeoutForNodeEvaluationInMS = pTimeoutInMS;
		this.fallbackNodeEvaluator = pFallbackNodeEvaluator;
	}
	
	protected abstract V fTimeouted(Node<T, ?> node, int timeoutInMS) throws NodeEvaluationException, InterruptedException;

	@Override
	public final V f(Node<T, ?> node) throws NodeEvaluationException, InterruptedException {
		
		/* determine time available and granted for node evaluation */
		int remainingTime;
		if (totalDeadline >= 0 && timeoutForNodeEvaluationInMS >= 0) {
			remainingTime = Math.min(timeoutForNodeEvaluationInMS, (int)(totalDeadline - System.currentTimeMillis()));
		}
		else if (totalDeadline >= 0) {
			remainingTime = (int)(totalDeadline - System.currentTimeMillis());
		}
		else if (timeoutForNodeEvaluationInMS >= 0) {
			remainingTime = timeoutForNodeEvaluationInMS;
		}
		else {
			remainingTime = Integer.MAX_VALUE - 1000;
		}
		int grantedTime = remainingTime - 50;
		int interruptionTime = remainingTime + 150;
		
		/* execute evaluation */
		AtomicBoolean controlledInterrupt = new AtomicBoolean(false);
		TimeoutSubmitter ts = TimeoutTimer.getInstance().getSubmitter();
		int taskId = ts.interruptMeAfterMS(interruptionTime, () -> controlledInterrupt.set(true));
		try {
			V result = fTimeouted(node, grantedTime);
			ts.cancelTimeout(taskId);
			ts.close();
			return result;
		} catch (InterruptedException e) {
			Thread.interrupted(); // clear interrupted field
			if (controlledInterrupt.get())
				return fallbackNodeEvaluator.f(node);
			else
				throw e;
		}
	}

	public int getTimeoutForNodeEvaluationInMS() {
		return timeoutForNodeEvaluationInMS;
	}

	public INodeEvaluator<T, V> getFallbackNodeEvaluator() {
		return fallbackNodeEvaluator;
	}


	public long getTotalDeadline() {
		return this.totalDeadline;
	}

	public void setTotalDeadline(final long totalDeadline) {
		this.totalDeadline = totalDeadline;
	}
	
	protected void checkInterruption() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Node evaluation of " + this.getClass().getName() + " has been interrupted.");
		}
	}
}
