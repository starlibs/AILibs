package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

/**
 * A* algorithm implementation using the method design pattern.
 *
 * @author Felix Mohr
 */
public class BestFirstEpsilon<T, A, W extends Comparable<W>> extends StandardBestFirst<T, A, Double> {

	private Logger logger = LoggerFactory.getLogger(BestFirstEpsilon.class);
	private String loggerName;

	private final IPathEvaluator<T, A, W> secondaryNodeEvaluator;
	private final Map<BackPointerPath<T, A, Double>, W> secondaryCache = new HashMap<>();
	private final OpenList focalBasedOpenList = new OpenList();
	private final boolean absolute;
	private final double epsilon;

	@SuppressWarnings("serial")
	private class OpenList extends PriorityQueue<BackPointerPath<T, A, Double>> {

		@Override
		public BackPointerPath<T, A, Double> peek() {
			if (BestFirstEpsilon.this.epsilon <= 0 || BestFirstEpsilon.this.open.isEmpty()) {
				return super.peek();
			}

			/* build focal list and compute secondary f values for the elements in the list */
			double best = super.peek().getScore();
			double threshold = (BestFirstEpsilon.this.absolute ? (best >= 0 ? best + BestFirstEpsilon.this.epsilon : best - BestFirstEpsilon.this.epsilon)
					: best * (best >= 0 ? 1 + BestFirstEpsilon.this.epsilon : 1 - BestFirstEpsilon.this.epsilon));
			Collection<BackPointerPath<T, A, Double>> focal = super.stream().filter(n -> n.getScore() <= threshold).collect(Collectors.toList());
			focal.stream().filter(n -> !BestFirstEpsilon.this.secondaryCache.containsKey(n)).forEach(n -> {
				try {
					BestFirstEpsilon.this.secondaryCache.put(n, BestFirstEpsilon.this.secondaryNodeEvaluator.evaluate(n));
				} catch (Exception e) {
					BestFirstEpsilon.this.logger.error("Observed exception during computation of f: {}", e);
				}
			});
			Optional<BackPointerPath<T, A, Double>> choice = focal.stream().min((p1, p2) -> BestFirstEpsilon.this.secondaryCache.get(p1).compareTo(BestFirstEpsilon.this.secondaryCache.get(p2)));
			if (!choice.isPresent()) {
				throw new IllegalStateException("No choice found!");
			}
			BestFirstEpsilon.this.logger.info("Best score is {}. Threshold for focal is {}. Choose node with f1 {} and best f2 {}. Size of focal was {}.", best, threshold, choice.get().getScore(),
					BestFirstEpsilon.this.secondaryCache.get(choice.get()), focal.size());
			return choice.get();
		}
	}

	public BestFirstEpsilon(final GraphSearchWithSubpathEvaluationsInput<T, A, Double> problem, final IPathEvaluator<T, A, W> pSecondaryNodeEvaluator, final int epsilon) {
		this(problem, pSecondaryNodeEvaluator, epsilon, true);
	}

	public BestFirstEpsilon(final GraphSearchWithSubpathEvaluationsInput<T, A, Double> problem, final IPathEvaluator<T, A, W> pSecondaryNodeEvaluator, final double epsilon, final boolean absolute) {
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