package ai.libs.jaicore.search.algorithms.standard.mcts.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.search.algorithms.standard.mcts.AUpdatingPolicy;

public class TAGPolicy<T, A> extends AUpdatingPolicy<T, A> implements ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(TAGPolicy.class);
	private double explorationConstant = Math.sqrt(2);
	private final int s = 10;
	private final double delta = 0.01; // must be smaller than 1

	public TAGPolicy() {
		super();
	}

	public TAGPolicy(final boolean maximize) {
		super(maximize);
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		super.setLoggerName(name + "._updating");
		this.logger = LoggerFactory.getLogger(name);
	}

	private List<Double> getBestScores(final double[] scores, final int k) {
		List<Double> vals = new ArrayList<>();
		for (double score : scores) {
			vals.add(score);
		}
		return vals.stream().sorted().limit(k).collect(Collectors.toList());
	}

	private List<Double> getScoresNotWorseThanThreshold(final double[] scores, final double threshold) {
		List<Double> vals = new ArrayList<>();
		for (double score : scores) {
			if (score <= threshold) {
				vals.add(score);
			}
		}
		return vals;
	}


	@Override
	public double getScore(final NodeLabel labelOfNode, final NodeLabel labelOfChild) {

		List<Double> bestScoresObservedInparent = this.getBestScores(labelOfNode.scores.getValues(), this.s);
		double threshold = bestScoresObservedInparent.get(bestScoresObservedInparent.size() - 1);
		List<Double> relevantScoresOfChild = this.getScoresNotWorseThanThreshold(labelOfChild.scores.getValues(), threshold);
		int sChild = relevantScoresOfChild.size();
		//		System.out.println("Best score is " + threshold + ". " + relevantScoresOfChild.size() + "/" + labelOfChild.scores.getN() + " scores of the child are relevant. All scores: " + Arrays.toString(labelOfChild.scores.getSortedValues()));
		double k = 2; // TODO: This is the number of child nodes, SHOULD NOT BE CONSTANT!
		double alpha = Math.log(2 * labelOfNode.scores.getN() * k / this.delta);
		if (alpha < 0) {
			throw new IllegalStateException("Alpha must not be negative. Check delta value (must be smaller than 1)");
		}
		int childVisits = (int)labelOfChild.scores.getN();
		if (childVisits == 0) {
			throw new IllegalArgumentException("Cannot compute score for child with no visits!");
		}

		Double h = (sChild + alpha + Math.sqrt(2 * sChild * alpha + Math.pow(alpha, 2))) / childVisits;
		//		System.out.println("(" + sChild + " + " + alpha + " + sqrt(2 * " + sChild + " * " + alpha + " + " + Math.pow(alpha, 2) + ") / " + childVisits);

		this.logger.trace("Compute TAG score of {}", h);
		return h;
	}

	public double getExplorationConstant() {
		return this.explorationConstant;
	}

	public void setExplorationConstant(final double explorationConstant) {
		this.explorationConstant = explorationConstant;
	}

	@Override
	public A getActionBasedOnScores(final Map<A, Double> scores) {
		A choice = null;
		this.logger.debug("Getting action for scores {}", scores);
		double best = -1;
		for (Entry<A, Double> entry : scores.entrySet()) {
			A action = entry.getKey();
			Double score = entry.getValue();
			if (score.isNaN()) {
				throw new IllegalStateException("Score for option " + action + " is NaN");
			}
			if (score > best) {
				this.logger.trace("Updating best choice {} with {} since it is better than the current solution with performance {}", choice, action, best);
				best = score;
				choice = action;
			} else {
				this.logger.trace("Skipping current solution {} since its score {} is not better than the currently best {}.", action, score, best);
			}
		}
		return choice;
	}
}
