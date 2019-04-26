package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.model.travesaltree.Node;
import jaicore.timing.TimedComputation;

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
		try {
			return TimedComputation.compute(() -> this.fTimeouted(node, grantedTime), interruptionTime, "Node evaluation has timed out (" + TimeAwareNodeEvaluator.class.getName() + "::" + Thread.currentThread() + "-" + System.currentTimeMillis() + ")");
		} catch (TimeoutException e) {
			return this.fallbackNodeEvaluator.f(node);
		}
		catch (ExecutionException e) {
			throw (NodeEvaluationException)e.getCause();
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
