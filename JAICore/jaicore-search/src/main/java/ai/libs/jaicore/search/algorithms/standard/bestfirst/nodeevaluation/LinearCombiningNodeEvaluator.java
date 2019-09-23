package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.Map;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.IPath;

public class LinearCombiningNodeEvaluator<T, A> implements IPathEvaluator<T, A, Double> {

	private final Map<IPathEvaluator<T, A, Double>, Double> evaluators;

	public LinearCombiningNodeEvaluator(final Map<IPathEvaluator<T, A, Double>, Double> evaluators) {
		super();
		this.evaluators = evaluators;
	}

	@Override
	public Double evaluate(final IPath<T, A> path) throws PathEvaluationException, InterruptedException {
		double score = 0;
		double incr;
		for (IPathEvaluator<T, A, Double> evaluator : this.evaluators.keySet()) {
			if (this.evaluators.get(evaluator) != 0) {
				incr = evaluator.evaluate(path);
				if (incr == Integer.MAX_VALUE) {
					score = Integer.MAX_VALUE;
					break;
				} else {
					score += incr * this.evaluators.get(evaluator);
				}
			}
		}
		return score;
	}
}
