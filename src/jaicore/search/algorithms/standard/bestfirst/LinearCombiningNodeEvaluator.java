package jaicore.search.algorithms.standard.bestfirst;

import java.util.Map;

import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.Node;

public class LinearCombiningNodeEvaluator<T> implements INodeEvaluator<T,BestFirstEpsilonLabel> {

	private final Map<INodeEvaluator<T,BestFirstEpsilonLabel>, Double> evaluators;

	public LinearCombiningNodeEvaluator(Map<INodeEvaluator<T,BestFirstEpsilonLabel>, Double> evaluators) {
		super();
		this.evaluators = evaluators;
	}

	@Override
	public BestFirstEpsilonLabel f(Node<T,?> node) throws Exception {
		double score = 0;
		int incr;
		for (INodeEvaluator<T,BestFirstEpsilonLabel> evaluator : evaluators.keySet()) {
			if (evaluators.get(evaluator) != 0) {
				incr = evaluator.f(node).getF1();
				if (incr == Integer.MAX_VALUE) {
					score = Integer.MAX_VALUE;
					break;
				}
				else
					score += incr * evaluators.get(evaluator);
			}
		}
		return new BestFirstEpsilonLabel((int)Math.round(score), 0);
	}
}
