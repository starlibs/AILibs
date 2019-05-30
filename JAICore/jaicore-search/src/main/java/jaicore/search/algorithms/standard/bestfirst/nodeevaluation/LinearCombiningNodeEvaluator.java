package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.Map;

import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.model.travesaltree.Node;

public class LinearCombiningNodeEvaluator<T> implements INodeEvaluator<T,Double> {

	private final Map<INodeEvaluator<T,Double>, Double> evaluators;

	public LinearCombiningNodeEvaluator(Map<INodeEvaluator<T,Double>, Double> evaluators) {
		super();
		this.evaluators = evaluators;
	}

	@Override
	public Double f(Node<T,?> node) throws NodeEvaluationException, InterruptedException  {
		double score = 0;
		double incr;
		for (INodeEvaluator<T,Double> evaluator : evaluators.keySet()) {
			if (evaluators.get(evaluator) != 0) {
				incr = evaluator.f(node);
				if (incr == Integer.MAX_VALUE) {
					score = Integer.MAX_VALUE;
					break;
				}
				else
					score += incr * evaluators.get(evaluator);
			}
		}
		return score;
	}
}
