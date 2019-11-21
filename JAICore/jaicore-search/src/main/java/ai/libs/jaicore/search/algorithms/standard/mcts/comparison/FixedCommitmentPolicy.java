package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.search.algorithms.standard.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPathUpdatablePolicy;

public class FixedCommitmentPolicy<N, A> implements IPathUpdatablePolicy<N, A, Double> {
	private final Map<N, DescriptiveStatistics> observationsPerNode = new HashMap<>();

	private final int k;
	private final Function<DescriptiveStatistics, Double> metric;

	public FixedCommitmentPolicy(final int k, final Function<DescriptiveStatistics, Double> metric) {
		super();
		this.k = k;
		this.metric = metric;
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
			DescriptiveStatistics observations = this.observationsPerNode.computeIfAbsent(child.getValue(), n -> new DescriptiveStatistics());
			int numOfVisitsOfThisChild = (int)observations.getN();
			if (numOfVisitsOfThisChild < numOfVisitsOfThatChild) {
				actionAndChildWithLeastVisits = child;
				numOfVisitsOfThatChild = numOfVisitsOfThisChild;
			}
			double bestScoreOfThisChild = this.metric.apply(observations);
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
	public void updatePath(final IPath<N, A> path, final Double playout, final int pathLength) {
		for (N node : path.getNodes()) {
			DescriptiveStatistics statsOfNode = this.observationsPerNode.computeIfAbsent(node, n -> new DescriptiveStatistics());
			statsOfNode.addValue(playout);
		}
	}
}
