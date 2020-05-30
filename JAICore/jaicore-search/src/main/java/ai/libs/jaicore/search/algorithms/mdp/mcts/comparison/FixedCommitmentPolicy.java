package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.search.algorithms.mdp.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.mdp.mcts.IPathUpdatablePolicy;

public class FixedCommitmentPolicy<N, A> implements IPathUpdatablePolicy<N, A, Double> {
	private final Map<N, Map<A, DescriptiveStatistics>> observationsPerNode = new HashMap<>();

	private final int k;
	private final ToDoubleFunction<DescriptiveStatistics> metric;

	public FixedCommitmentPolicy(final int k, final ToDoubleFunction<DescriptiveStatistics> metric) {
		super();
		this.k = k;
		this.metric = metric;
	}

	@Override
	public A getAction(final N node, final Collection<A> actions) throws ActionPredictionFailedException {

		/* determine number of visits of the child with least visits */
		A actionWithLeastVisits = null;
		A actionWithBestVisit = null;
		int numOfVisitsOfThatChild = Integer.MAX_VALUE;
		double bestChildScore = Double.MAX_VALUE;
		for (A action: actions) {
			DescriptiveStatistics observations = this.observationsPerNode.computeIfAbsent(node, n -> new HashMap<>()).computeIfAbsent(action, a -> new DescriptiveStatistics());
			int numOfVisitsOfThisChild = (int)observations.getN();
			if (numOfVisitsOfThisChild < numOfVisitsOfThatChild) {
				actionWithLeastVisits = action;
				numOfVisitsOfThatChild = numOfVisitsOfThisChild;
			}
			double bestScoreOfThisChild = this.metric.applyAsDouble(observations);
			if (bestScoreOfThisChild < bestChildScore) {
				bestChildScore = bestScoreOfThisChild;
				actionWithBestVisit = action;
			}
		}

		/* now decide */
		Objects.requireNonNull(actionWithLeastVisits);
		Objects.requireNonNull(actionWithBestVisit);
		if (numOfVisitsOfThatChild < this.k) {
			return actionWithLeastVisits;
		} else {
			return actionWithBestVisit;
		}
	}

	@Override
	public void updatePath(final ILabeledPath<N, A> path, final List<Double> scores) {
		List<N> nodes = path.getNodes();
		List<A> arcs = path.getArcs();
		int l = nodes.size();
		double accumulatedScores = 0;
		for (int i = l - 2; i >= 0; i--) {
			N node = nodes.get(i);
			A action = arcs.get(i);
			accumulatedScores += scores.get(i);
			DescriptiveStatistics statsForNodeActionPair = this.observationsPerNode.computeIfAbsent(node, n -> new HashMap<>()).computeIfAbsent(action,a -> new DescriptiveStatistics());
			statsForNodeActionPair.addValue(accumulatedScores);
		}
	}
}
