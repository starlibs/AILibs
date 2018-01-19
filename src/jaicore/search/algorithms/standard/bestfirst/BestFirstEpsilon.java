package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;

/**
 * A* algorithm implementation using the method design pattern.
 *
 * @author Felix Mohr
 */
public class BestFirstEpsilon<T, A> extends ORGraphSearch<T, A, BestFirstEpsilonLabel> {

	private final boolean absolute;
	private final double epsilon;

	
	public BestFirstEpsilon(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, BestFirstEpsilonLabel> pNodeEvaluator, int epsilon) {
		this(graphGenerator, pNodeEvaluator, epsilon, true);
	}
	
	public BestFirstEpsilon(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, BestFirstEpsilonLabel> pNodeEvaluator, double epsilon, boolean absolute) {
		super(graphGenerator, pNodeEvaluator);
		this.epsilon = epsilon;
		this.absolute = absolute;
		
		/* overwrite node selector */
		this.setNodeSelector(open -> {
			if (epsilon <= 0 || open.isEmpty())
				return open.poll();
			int best = open.peek().getInternalLabel().getF1();
			double threshold = (absolute ? (best >= 0 ? best + epsilon : best - epsilon) : best * (best >= 0 ? 1 + epsilon : 1 - epsilon));
			Node<T, BestFirstEpsilonLabel> choice = open.stream().filter(n -> n.getInternalLabel().getF1() <= threshold)
					.min((p1, p2) -> p1.getInternalLabel().getF2() - p2.getInternalLabel().getF2()).get();
			open.remove(choice);
			return choice;
		});
	}

	public boolean isAbsolute() {
		return absolute;
	}

	public double getEpsilon() {
		return epsilon;
	}
}