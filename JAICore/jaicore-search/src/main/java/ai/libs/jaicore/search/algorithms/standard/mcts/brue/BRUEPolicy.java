package ai.libs.jaicore.search.algorithms.standard.mcts.brue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.search.algorithms.standard.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPathUpdatablePolicy;

/**
 *
 * This policy implements the BRUE algorithm presented in
 *
 * @article{
  	title={Simple regret optimization in online planning for Markov decision processes},
  	author={Feldman, Zohar and Domshlak, Carmel},
  	journal={Journal of Artificial Intelligence Research},
  	volume={51},
  	pages={165--205},
  	year={2014}
	}
 *
 * @author Felix Mohr
 *
 * @param <N>
 * @param <A>
 */
public class BRUEPolicy<N, A> implements IPathUpdatablePolicy<N, A, Double> {

	private final Map<Pair<N, A>, Integer> nCounter = new HashMap<>();
	private final Map<Pair<N, A>, Double> qHat = new HashMap<>();
	private final Random random;
	private final int timeHorizon; // H in the paper
	private final boolean maximize;
	private int n = 0; // iteration of the algorithm

	public BRUEPolicy(final boolean maximize, final int timeHorizon, final Random random) {
		super();
		this.maximize = maximize;
		this.timeHorizon = timeHorizon;
		this.random = random;
	}

	public BRUEPolicy(final boolean maximize) {
		this(maximize, 1000, new Random(0));
	}

	@Override
	public A getAction(final N node, final Map<A, N> actionsWithSuccessors) throws ActionPredictionFailedException {
		if (actionsWithSuccessors.isEmpty()) {
			throw new IllegalArgumentException();
		}

		/* compute actions that are optimal for this state */
		double bestScore = (this.maximize ? -1 : 1) * Double.MAX_VALUE;
		double worstValue = bestScore;
		List<A> bestActions = new ArrayList<>();
		for (Entry<A, N> entry : actionsWithSuccessors.entrySet()) {
			Pair<N, A> pair = new Pair<>(entry.getValue(), entry.getKey());
			double score = this.qHat.containsKey(pair) ? this.qHat.get(pair) : worstValue;
			if (score < bestScore) {
				bestActions.clear();
				bestScore = score;
				bestActions.add(pair.getY());
			}
			else if (score == bestScore) {
				bestActions.add(pair.getY());
			}
		}

		/* draw uniformly from them */
		if (bestActions.isEmpty()) {
			throw new IllegalStateException();
		}
		if (bestActions.size() > 1) {
			Collections.shuffle(bestActions, this.random);
		}
		A choice = bestActions.get(0);
		Pair<N, A> pair = new Pair<>(node, choice);
		this.nCounter.put(pair, this.nCounter.computeIfAbsent(pair, p -> 0) + 1);
		return choice;
	}

	@Override
	public void updatePath(final ILabeledPath<N, A> path, final Double playoutScore, final int lengthOfPlayoutPath) {

		/* BRUE only updates the state previous to the switch point. Compute this state */
		int sigmaN = this.getSwitchingPoint(this.n);
		this.n++;
		if (sigmaN > path.getNumberOfNodes() - 2) {
			return;
		}
		N node = path.getNodes().get(sigmaN - 1); // where we come from
		A arc = path.getArcs().get(sigmaN - 1); // the action we take
		Pair<N, A> updatedPair = new Pair<>(node, arc);

		/* update the state */
		double worstValue = (this.maximize ? (-1) : 1) * Double.MAX_VALUE;
		double currentScore = this.qHat.containsKey(updatedPair) ? this.qHat.get(updatedPair) : worstValue;
		if (!this.nCounter.containsKey(updatedPair)) {
			throw new IllegalStateException("No visit stats for updated pair " + updatedPair + " available.");
		}
		double newScore = currentScore + (playoutScore - currentScore) / this.nCounter.get(updatedPair);
		this.qHat.put(updatedPair, newScore);
	}

	public int getSwitchingPoint(final int n) {
		return this.timeHorizon - ((n-1) % this.timeHorizon);
	}
}
