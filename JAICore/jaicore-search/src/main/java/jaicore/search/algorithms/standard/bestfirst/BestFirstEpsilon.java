package jaicore.search.algorithms.standard.bestfirst;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

/**
 * A* algorithm implementation using the method design pattern.
 *
 * @author Felix Mohr
 */
public class BestFirstEpsilon<T, A, W extends Comparable<W>> extends StandardBestFirst<T, A, Double> {

	private Logger logger = LoggerFactory.getLogger(BestFirst.class);
	private String loggerName;

	private final INodeEvaluator<T, W> secondaryNodeEvaluator;
	private final Map<Node<T, Double>, W> secondaryCache = new HashMap<>();
	private final OpenList focalBasedOpenList = new OpenList();
	private final boolean absolute;
	private final double epsilon;

	@SuppressWarnings("serial")
	private class OpenList extends PriorityQueue<Node<T, Double>> {

		@Override
		public Node<T, Double> peek() {
			if (BestFirstEpsilon.this.epsilon <= 0 || BestFirstEpsilon.this.open.isEmpty()) {
				return super.peek();
			}

			/* build focal list and compute secondary f values for the elements in the list */
			double best = super.peek().getInternalLabel();
			double threshold = (BestFirstEpsilon.this.absolute ? (best >= 0 ? best + BestFirstEpsilon.this.epsilon : best - BestFirstEpsilon.this.epsilon)
					: best * (best >= 0 ? 1 + BestFirstEpsilon.this.epsilon : 1 - BestFirstEpsilon.this.epsilon));
			Collection<Node<T, Double>> focal = super.stream().filter(n -> n.getInternalLabel() <= threshold).collect(Collectors.toList());
			focal.stream().filter(n -> !BestFirstEpsilon.this.secondaryCache.containsKey(n)).forEach(n -> {
				try {
					BestFirstEpsilon.this.secondaryCache.put(n, BestFirstEpsilon.this.secondaryNodeEvaluator.f(n));
				} catch (Throwable e) {
					e.printStackTrace();
				}
			});
			Node<T, Double> choice = focal.stream().min((p1, p2) -> BestFirstEpsilon.this.secondaryCache.get(p1).compareTo(BestFirstEpsilon.this.secondaryCache.get(p2))).get();
			BestFirstEpsilon.this.logger.info("Best score is {}. Threshold for focal is {}. Choose node with f1 {} and best f2 {}. Size of focal was {}.", best, threshold, choice.getInternalLabel(),
					BestFirstEpsilon.this.secondaryCache.get(choice), focal.size());
			return choice;
		}
	}

	public BestFirstEpsilon(final GraphSearchWithSubpathEvaluationsInput<T, A, Double> problem, final INodeEvaluator<T, W> pSecondaryNodeEvaluator, final int epsilon) throws InterruptedException {
		this(problem, pSecondaryNodeEvaluator, epsilon, true);
	}

	public BestFirstEpsilon(final GraphSearchWithSubpathEvaluationsInput<T, A, Double> problem, final INodeEvaluator<T, W> pSecondaryNodeEvaluator, final double epsilon, final boolean absolute) throws InterruptedException {
		super(problem);
		this.secondaryNodeEvaluator = pSecondaryNodeEvaluator;
		this.epsilon = epsilon;
		this.absolute = absolute;

		/* overwrite node selector */
		this.setOpen(this.focalBasedOpenList);
	}

	public boolean isAbsolute() {
		return this.absolute;
	}

	public double getEpsilon() {
		return this.epsilon;
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		if (this.secondaryNodeEvaluator instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.secondaryNodeEvaluator).setLoggerName(name + ".secnodeeval");
		}
		super.setLoggerName(this.loggerName + "._bestfirst");
	}
}