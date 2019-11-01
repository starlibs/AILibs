package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.preferencekernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IPreferenceKernel;

public class BootstrappingPreferenceKernel<N, A> implements IPreferenceKernel<N, A> {

	private LabeledGraph<N, A> explorationGraph;
	private final Map<N, DescriptiveStatistics> observations = new HashMap<>();
	private final IBootstrappingParameterComputer bootstrapParameterComputer;

	private final int maxNumSamples = 100;
	private final int numBootstraps = 100;
	private final Random random = new Random(0);
	private final Map<N, List<List<N>>> rankingsForNodes = new HashMap<>();
	private final int minSamplesToCreateRankings;

	public BootstrappingPreferenceKernel(final IBootstrappingParameterComputer bootstrapParameterComputer, final int minSamplesToCreateRankings) {
		super();
		this.bootstrapParameterComputer = bootstrapParameterComputer;
		this.minSamplesToCreateRankings = minSamplesToCreateRankings;
	}

	@Override
	public void signalNewScore(final IPath<N, A> path, final double newScore) {

		/* add the observation to all stats on the path */
		List<N> nodes = path.getNodes();
		int l = nodes.size();
		for (int i = l - 1; i >= 0; i --) {
			N node = nodes.get(i);
			this.observations.computeIfAbsent(node, n -> new DescriptiveStatistics()).addValue(newScore);
			this.rankingsForNodes.put(node,this.drawNewRankingsForChildrenOfNode(node, this.bootstrapParameterComputer));
		}
	}

	/**
	 * Computes new rankings from a fresh bootstrap
	 *
	 * @param node
	 * @param parameterComputer
	 * @return
	 */
	public List<List<N>> drawNewRankingsForChildrenOfNode(final N node, final IBootstrappingParameterComputer parameterComputer) {
		List<List<N>> rankings = new ArrayList<>(this.numBootstraps);
		Collection<N> children = this.explorationGraph.getSuccessors(node);
		Map<N, double[]> observationsPerChild = new HashMap<>();
		for (N child : children) {
			if (!this.observations.containsKey(child)) {
				return null;
			}
			observationsPerChild.put(child, this.observations.get(child).getValues());
		}

		for (int bootstrap = 0; bootstrap < this.numBootstraps; bootstrap++) {
			Map<N, Double> scorePerChild = new HashMap<>();
			for (N child : children) {
				double[] observedScoresForChild = observationsPerChild.get(child);
				DescriptiveStatistics statsForThisChild = new DescriptiveStatistics();
				for (int sample = 0; sample < this.maxNumSamples; sample++) {
					statsForThisChild.addValue(observedScoresForChild[this.random.nextInt(observedScoresForChild.length)]);
				}
				scorePerChild.put(child, parameterComputer.getParameter(statsForThisChild));
			}
			List<N> ranking = children.stream().sorted((c1,c2) -> Double.compare(scorePerChild.get(c1), scorePerChild.get(c2))).collect(Collectors.toList());
			rankings.add(ranking);
		}
		return rankings;
	}

	@Override
	public List<List<N>> getRankingsForChildrenOfNode(final N node) {
		return this.rankingsForNodes.get(node);
	}

	@Override
	public void setExplorationGraph(final LabeledGraph<N, A> graph) {
		this.explorationGraph = graph;
	}

	@Override
	public boolean canProduceReliableRankings(final N node) {
		int minObservations = Integer.MAX_VALUE;
		for (N child : this.explorationGraph.getSuccessors(node)) {
			minObservations = (int)Math.min(minObservations, this.observations.get(child).getN());
		}
		return minObservations > this.minSamplesToCreateRankings;
	}
}
