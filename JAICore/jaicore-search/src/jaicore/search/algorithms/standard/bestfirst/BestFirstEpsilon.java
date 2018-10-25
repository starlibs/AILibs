package jaicore.search.algorithms.standard.bestfirst;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;

/**
 * A* algorithm implementation using the method design pattern.
 *
 * @author Felix Mohr
 */
public class BestFirstEpsilon<T, A, W extends Comparable<W>> extends StandardBestFirst<T, A, Double> {
	
	private final static Logger logger = LoggerFactory.getLogger(BestFirstEpsilon.class);

	private final INodeEvaluator<T, W> secondaryNodeEvaluator;
	private final Map<Node<T,Double>, W> secondaryCache = new HashMap<>();
	private final OpenList focalBasedOpenList = new OpenList();
	private final boolean absolute;
	private final double epsilon;
	
	@SuppressWarnings("serial")
	private class OpenList extends PriorityQueue<Node<T,Double>> {
		
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
				} catch (Throwable e) {
					e.printStackTrace();
				}
			});
			Node<T, Double> choice = focal.stream().min((p1, p2) -> secondaryCache.get(p1).compareTo(secondaryCache.get(p2))).get();
			logger.info("Best score is {}. Threshold for focal is {}. Choose node with f1 {} and best f2 {}. Size of focal was {}.", best, threshold, choice.getInternalLabel(), secondaryCache.get(choice), focal.size());
			return choice;
		}
	}

	
	public BestFirstEpsilon(GeneralEvaluatedTraversalTree<T, A, Double> problem, INodeEvaluator<T, W> pSecondaryNodeEvaluator, int epsilon) throws InterruptedException {
		this(problem, pSecondaryNodeEvaluator, epsilon, true);
	}
	
	public BestFirstEpsilon(GeneralEvaluatedTraversalTree<T, A, Double> problem, INodeEvaluator<T, W> pSecondaryNodeEvaluator, double epsilon, boolean absolute) throws InterruptedException {
		super(problem);
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