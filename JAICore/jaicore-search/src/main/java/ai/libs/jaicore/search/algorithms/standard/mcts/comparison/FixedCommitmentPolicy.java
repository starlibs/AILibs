package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ai.libs.jaicore.search.algorithms.standard.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPathUpdatablePolicy;

public class FixedCommitmentPolicy<N, A> implements IPathUpdatablePolicy<N, A, Double> {
	private final Map<N, Integer> numObservationsPerNode = new HashMap<>();
	private final Map<N, Double> observedMin = new HashMap<>();

	private final int k;

	public FixedCommitmentPolicy(final int k) {
		super();
		this.k = k;
	}

	@Override
	public A getAction(final N node, final Map<A, N> actionsWithSuccessors) throws ActionPredictionFailedException {

		long start = System.currentTimeMillis();

		/* determine number of visits of the child with least visits */
		Entry<A, N> actionAndChildWithLeastVisits = null;
		Entry<A, N> actionAndChildWithBestVisit = null;
		int numOfVisitsOfThatChild = Integer.MAX_VALUE;
		double bestChildScore = Double.MAX_VALUE;
		for (Entry<A, N> child : actionsWithSuccessors.entrySet()) {
			int numOfVisitsOfThisChild = this.numObservationsPerNode.computeIfAbsent(child.getValue(), n -> 0);
			if (numOfVisitsOfThisChild < numOfVisitsOfThatChild) {
				actionAndChildWithLeastVisits = child;
				numOfVisitsOfThatChild = numOfVisitsOfThisChild;
			}
			double bestScoreOfThisChild = this.observedMin.computeIfAbsent(child.getValue(), n -> Double.MAX_VALUE);
			if (bestScoreOfThisChild < bestChildScore) {
				bestChildScore = bestScoreOfThisChild;
				actionAndChildWithBestVisit = child;
			}
		}

		/* now decide */
		if (numOfVisitsOfThatChild < this.k) {
			return actionAndChildWithLeastVisits.getKey();
		} else {
			return actionAndChildWithBestVisit.getKey();
		}
	}

	@Override
	public void updatePath(final List<N> path, final Double playout) {
		for (N node : path) {
			int numObservations = this.numObservationsPerNode.computeIfAbsent(node, n -> 0);
			double bestScoreUpToNow = this.observedMin.computeIfAbsent(node, n -> Double.MAX_VALUE);
			this.numObservationsPerNode.put(node, numObservations + 1);
			if (bestScoreUpToNow > playout) {
				this.observedMin.put(node, playout);
			}
		}
	}
}
