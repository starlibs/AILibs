package jaicore.search.algorithms.standard.bestfirst;

import java.util.Map;

import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.Node;

public class LinearCombiningNodeEvaluator<T> implements INodeEvaluator<T,Integer> {

	private final Map<INodeEvaluator<T,Integer>, Double> evaluators;

	public LinearCombiningNodeEvaluator(Map<INodeEvaluator<T,Integer>, Double> evaluators) {
		super();
		this.evaluators = evaluators;
	}

	@Override
	public Integer f(Node<T,Integer> node) throws Exception {
		double score = 0;
		int incr;
		for (INodeEvaluator<T,Integer> evaluator : evaluators.keySet()) {
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
		return (int)Math.round(score);
	}
}
