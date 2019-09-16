package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.math.linearalgebra.AffineFunction;

/**
 * This policy is based on the Plackett Luce model.
 * Every node gets a skill parameter, and the score of a node is the relative skill among the skills of all siblings
 *
 * @author felix
 *
 * @param <N>
 * @param <A>
 */
public class PLKPolicy<N, A> extends AUpdatingPolicy<N, A> implements ILoggingCustomizable {
	private static final double UNCERTAINTY_DISCOUNT = 0.9;
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(PLKPolicy.class);
	private final int k;
	private final Random random;
	private final int epochs;
	private final AffineFunction gamma;
	private int evaluations;

	public PLKPolicy(final int k, final Random random, final int epochs) {
		super();
		this.k = k;
		this.random = random;
		this.epochs = epochs;
		this.gamma = new AffineFunction(1, 1, 100, 0);
	}

	public PLKPolicy(final int k, final Random random, final int epochs, final boolean maximize) {
		super(maximize);
		this.k = k;
		this.random = random;
		this.epochs = epochs;
		this.gamma = new AffineFunction(1, 1, 100, 0);
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

	@Override
	public double getScore(final NodeLabel labelOfNode, final NodeLabel labelOfChild) {
		this.evaluations++;
		this.logger.info("Computing PL-{} score of a child node with label {}. Label of parent is {}", this.k, labelOfChild, labelOfNode);
		if (labelOfNode.visits < labelOfChild.visits) {
			throw new IllegalArgumentException("Number of visits in parent cannot be smaller than number of visits in child.");
		}
		if (labelOfNode.scores.getMin() > labelOfChild.scores.getMin()) {
			throw new IllegalArgumentException("Minimum in parent cannot be greater than minimum in child.");
		}
		if (labelOfNode.scores.getMax() < labelOfChild.scores.getMax()) {
			throw new IllegalArgumentException("Maximum in parent cannot be smaller than maximum in child.");
		}
		int n = (int)labelOfChild.scores.getN();

		/* first get mean of minimum k observations under this child */
		int m = Math.min(n, this.k);
		LinkedList<Double> observedValues = new LinkedList<>();
		for (int i = 0; i < n; i++) {
			observedValues.add(labelOfChild.scores.getElement(i));
		}
		Collections.sort(observedValues);
		DescriptiveStatistics subStats = new DescriptiveStatistics();
		for (int i = 0; i < m; i++) {
			double val = this.isMaximize() ? observedValues.removeLast() : observedValues.removeFirst();
			subStats.addValue(val);
		}
		double mean = subStats.getMean();

		/* now indirectly weight the mean with the number of visits we already had here, which should be a proxy for certainty */
		double distanceToBestObservedHere = this.isMaximize() ? (labelOfNode.scores.getMax() - mean)  : (mean - labelOfNode.scores.getMin());
		assert distanceToBestObservedHere >= 0 : "Distance cannot be negative! Mean is " + mean + ", best seen label is " + labelOfNode.scores.getMin();
		double exponent = 1.0 / n;
		double score = 1 - (this.isMaximize() ? 1 : Math.pow(distanceToBestObservedHere, exponent));
		this.logger.debug("Score is {} = 1 - ({})^{} where distance to minimum is {} and visits are {}", score, distanceToBestObservedHere, exponent, distanceToBestObservedHere, n);
		if (score < 0) {
			throw new IllegalStateException("Determined negative score " + score + ", which must not be the case.");
		}
		return score;
	}

	@Override
	public A getActionBasedOnScores(final Map<A, Double> scores) {
		List<A> actions = new ArrayList<>(scores.keySet());
		//		Map<A, Double> skills = new HashMap<>();
		//		for (A a : actions) {
		//			skills.put(a, this.isMaximize() ? scores.get(a) : 1 / scores.get(a));
		//			this.logger.debug("Skill of action {} is {}", a, skills.get(a));
		//		}
		List<Double> scoresPerAction = actions.stream().map(scores::get).collect(Collectors.toList());
		this.logger.debug("Scores of actions {} are {}", actions, scoresPerAction);
		double totalSize = scoresPerAction.stream().reduce(0.0, (ts, s) -> ts += s);
		double randomPosition = this.random.nextDouble() * totalSize;
		this.logger.debug("Random position is {}/{}", randomPosition, totalSize);
		double coveredSize = 0;
		A currentAction = null;
		Iterator<A> it = actions.iterator();
		do {
			currentAction = it.next();
			coveredSize += scores.get(currentAction);
		}
		while (coveredSize < randomPosition);
		this.logger.debug("Choosing index {}/{} with relative score {}", (actions.indexOf(currentAction) + 1), actions.size(), (scores.get(currentAction) / totalSize));
		return currentAction;
	}
}
