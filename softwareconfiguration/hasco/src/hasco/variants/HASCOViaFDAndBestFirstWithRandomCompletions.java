package hasco.variants;

import hasco.RefinementConfiguredSoftwareConfigurationProblem;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS;

public class HASCOViaFDAndBestFirstWithRandomCompletions<V extends Comparable<V>> extends HASCOViaFDAndBestFirst<V> {

	public HASCOViaFDAndBestFirstWithRandomCompletions(RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, int numSamples, int seed) {
		this(configurationProblem, numSamples, seed, null);
	}
	
	public HASCOViaFDAndBestFirstWithRandomCompletions(RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, int numSamples, int seed, INodeEvaluator<TFDNode, V> preferredNodeEvaluator) {
		super(configurationProblem, new GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS<>(preferredNodeEvaluator, seed, numSamples));
	}
}
