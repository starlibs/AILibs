package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
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
		} catch (AlgorithmTimeoutedException e) {
			logger.warn("Computation of f-value for {} failed due to exception {} with message {}", node, e.getClass().getName(), e.getMessage());
			return this.fallbackNodeEvaluator.f(node);
		}
		catch (InterruptedException e) {
			logger.warn("Got interrupted during node evaluation. Throwing an InterruptedException");
			throw e;
		}
		catch (ExecutionException e) {
			if (e.getCause() instanceof NodeEvaluationException)
				throw (NodeEvaluationException)e.getCause();
			else throw new NodeEvaluationException(e.getCause(), "Could not evaluate path.");
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
		boolean interrupted = Thread.currentThread().isInterrupted();
		logger.debug("Checking interruption of RCNE: {}", interrupted);
		if (interrupted) {
			Thread.interrupted(); // reset flag
			throw new InterruptedException("Node evaluation of " + this.getClass().getName() + " has been interrupted.");
		}
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched logger name to {}", name);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}
}
