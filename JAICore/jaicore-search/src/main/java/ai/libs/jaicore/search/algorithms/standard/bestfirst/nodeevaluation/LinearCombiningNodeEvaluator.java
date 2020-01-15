package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.Map;
import java.util.Map.Entry;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;

public class LinearCombiningNodeEvaluator<T, A> implements IPathEvaluator<T, A, Double> {

	private final Map<IPathEvaluator<T, A, Double>, Double> evaluatorsWithWeights;

	public LinearCombiningNodeEvaluator(final Map<IPathEvaluator<T, A, Double>, Double> evaluators) {
		super();
		this.evaluatorsWithWeights = evaluators;
	}

	@Override
	public Double evaluate(final ILabeledPath<T, A> path) throws PathEvaluationException, InterruptedException {
		double score = 0;
		double incr;
		for (Entry<IPathEvaluator<T, A, Double>, Double> evaluatorWeightPair : this.evaluatorsWithWeights.entrySet()) {
			if (evaluatorWeightPair.getValue() != 0) {
				incr = evaluatorWeightPair.getKey().evaluate(path);
				if (incr == Integer.MAX_VALUE) {
					score = Integer.MAX_VALUE;
					break;
				} else {
					score += incr * evaluatorWeightPair.getValue();
				}
			}
		}
		return score;
	}
}
