package jaicore.search.algorithms.standard.bestfirst;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.PriorityQueueOpen;

/**
 * A* algorithm implementation using the method design pattern.
 *
 * @author Felix Mohr
 */
public class BestFirstEpsilon<T, A, W extends Comparable<W>> extends ORGraphSearch<T, A, Double> {

	private final INodeEvaluator<T, W> secondaryNodeEvaluator;
	private final Map<Node<T,Double>, W> secondaryCache = new HashMap<>();
	private final OpenList focalBasedOpenList = new OpenList();
	private final boolean absolute;
	private final double epsilon;
	
	private class OpenList extends PriorityQueueOpen<Node<T,Double>> {
		
		@Override
		public Node<T,Double> peek() {
			if (epsilon <= 0 || open.isEmpty())
				return super.peek();
			
			/* build focal list and compute secondary f values for the elements in the list */
			double best = super.peek().getInternalLabel();
			double threshold = (absolute ? (best >= 0 ? best + epsilon : best - epsilon) : best * (best >= 0 ? 1 + epsilon : 1 - epsilon));
			Collection<Node<T,Double>> focal = super.stream().filter(n -> n.getInternalLabel() <= threshold).collect(Collectors.toList());
			focal.stream().filter(n -> !secondaryCache.containsKey(n)).forEach(n -> {
				try {
					secondaryCache.put(n, secondaryNodeEvaluator.f(n));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			Node<T, Double> choice = focal.stream().min((p1, p2) -> secondaryCache.get(p1).compareTo(secondaryCache.get(p2))).get();
			return choice;
		}
	}

	
	public BestFirstEpsilon(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, Double> pPrimaryNodeEvaluator, INodeEvaluator<T, W> pSecondaryNodeEvaluator, int epsilon) {
		this(graphGenerator, pPrimaryNodeEvaluator, pSecondaryNodeEvaluator, epsilon, true);
	}
	
	public BestFirstEpsilon(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, Double> pPrimaryNodeEvaluator, INodeEvaluator<T, W> pSecondaryNodeEvaluator, double epsilon, boolean absolute) {
		super(graphGenerator, pPrimaryNodeEvaluator);
		this.secondaryNodeEvaluator = pSecondaryNodeEvaluator;
		this.epsilon = epsilon;
		this.absolute = absolute;
		
		/* overwrite node selector */
		this.setOpen(focalBasedOpenList);
	}

	public boolean isAbsolute() {
		return absolute;
	}

	public double getEpsilon() {
		return epsilon;
	}
}