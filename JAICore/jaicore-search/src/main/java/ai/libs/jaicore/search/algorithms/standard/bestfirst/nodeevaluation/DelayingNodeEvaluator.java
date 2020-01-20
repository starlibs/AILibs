package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.Timeout;
import org.api4.java.datastructure.graph.ILabeledPath;

/**
 * This path evaluator can be used to artificially delay the computation of scores.
 * This can be a helpful property in the simulated benchmarking of algorithms for costly evaluations.
 *
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 * @param <V>
 */
public class DelayingNodeEvaluator<N, A, V extends Comparable<V>> extends DecoratingNodeEvaluator<N, A, V> {

	private final Timeout delay;

	public DelayingNodeEvaluator(final IPathEvaluator<N, A, V> evaluator, final Timeout delay) {
		super(evaluator);
		this.delay = delay;
	}

	@Override
	public V evaluate(final ILabeledPath<N, A> path) throws PathEvaluationException, InterruptedException {
		V score = super.getEvaluator().evaluate(path);
		Thread.sleep(this.delay.milliseconds());
		return score;
	}
}
