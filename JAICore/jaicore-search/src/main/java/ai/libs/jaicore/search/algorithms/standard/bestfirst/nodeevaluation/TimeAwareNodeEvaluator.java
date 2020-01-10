package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.concurrent.ExecutionException;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.timing.TimedComputation;

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
public abstract class TimeAwareNodeEvaluator<T, A, V extends Comparable<V>> implements IPathEvaluator<T, A, V>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(TimeAwareNodeEvaluator.class);
	private final int timeoutForNodeEvaluationInMS;
	private long totalDeadline = -1; // this deadline can be set to guarantee that there will be no activity after this timestamp
	private final IPathEvaluator<T, A, V> fallbackNodeEvaluator;

	public TimeAwareNodeEvaluator(final int pTimeoutInMS) {
		this(pTimeoutInMS, n -> null);
	}

	public TimeAwareNodeEvaluator(final int pTimeoutInMS, final IPathEvaluator<T, A, V> pFallbackNodeEvaluator) {
		super();
		this.timeoutForNodeEvaluationInMS = pTimeoutInMS;
		this.fallbackNodeEvaluator = pFallbackNodeEvaluator;
	}

	protected abstract V fTimeouted(ILabeledPath<T, A> node, int timeoutInMS) throws PathEvaluationException, InterruptedException;

	@Override
	public final V evaluate(final ILabeledPath<T, A> path) throws PathEvaluationException, InterruptedException {

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
			return TimedComputation.compute(() -> this.fTimeouted(path, grantedTime), interruptionTime,
					"Node evaluation has timed out (" + TimeAwareNodeEvaluator.class.getName() + "::" + Thread.currentThread() + "-" + System.currentTimeMillis() + ")");
		} catch (AlgorithmTimeoutedException e) {
			this.logger.warn("Computation of f-value for {} failed due to exception {} with message {}", path, e.getClass().getName(), e.getMessage());
			return this.fallbackNodeEvaluator.evaluate(path);
		} catch (InterruptedException e) {
			this.logger.warn("Got interrupted during node evaluation. Throwing an InterruptedException");
			throw e;
		} catch (ExecutionException e) {
			if (e.getCause() instanceof PathEvaluationException) {
				throw (PathEvaluationException) e.getCause();
			} else {
				throw new PathEvaluationException("Could not evaluate path.", e.getCause());
			}
		}
	}

	public int getTimeoutForNodeEvaluationInMS() {
		return this.timeoutForNodeEvaluationInMS;
	}

	public IPathEvaluator<T, A, V> getFallbackNodeEvaluator() {
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
		this.logger.debug("Checking interruption of RCNE: {}", interrupted);
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
