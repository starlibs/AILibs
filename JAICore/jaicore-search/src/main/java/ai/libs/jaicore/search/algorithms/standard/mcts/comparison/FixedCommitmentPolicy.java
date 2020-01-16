package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.search.algorithms.standard.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPathUpdatablePolicy;

public class FixedCommitmentPolicy<N, A> implements IPathUpdatablePolicy<N, A, Double> {
	private final Map<N, DescriptiveStatistics> observationsPerNode = new HashMap<>();

	private final int k;
	private final ToDoubleFunction<DescriptiveStatistics> metric;

	public FixedCommitmentPolicy(final int k, final ToDoubleFunction<DescriptiveStatistics> metric) {
		super();
		this.k = k;
		this.metric = metric;
	}

	@Override
	public A getAction(final N node, final Map<A, N> actionsWithSuccessors) throws ActionPredictionFailedException {

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
			double bestScoreOfThisChild = this.metric.applyAsDouble(observations);
			if (bestScoreOfThisChild < bestChildScore) {
				bestChildScore = bestScoreOfThisChild;
				actionAndChildWithBestVisit = child;
			}
		}

		/* now decide */
		Objects.requireNonNull(actionAndChildWithLeastVisits);
		Objects.requireNonNull(actionAndChildWithBestVisit);
		if (numOfVisitsOfThatChild < this.k) {
			return actionAndChildWithLeastVisits.getKey();
		} else {
			return actionAndChildWithBestVisit.getKey();
		}
	}

	@Override
	public void updatePath(final ILabeledPath<N, A> path, final Double playout, final int pathLength) {
		for (N node : path.getNodes()) {
			DescriptiveStatistics statsOfNode = this.observationsPerNode.computeIfAbsent(node, n -> new DescriptiveStatistics());
			statsOfNode.addValue(playout);
		}
	}
}
