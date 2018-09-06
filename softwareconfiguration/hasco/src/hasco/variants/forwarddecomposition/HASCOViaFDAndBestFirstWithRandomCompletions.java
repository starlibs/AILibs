package hasco.variants.forwarddecomposition;

import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS;

public class HASCOViaFDAndBestFirstWithRandomCompletions<V extends Comparable<V>> extends HASCOViaFDAndBestFirst<V> {

	public HASCOViaFDAndBestFirstWithRandomCompletions(RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, int numSamples, int seed, int timeoutForSingleCompletionEvaluationInMS, int timeoutForNodeEvaluationInMS) {
		this(configurationProblem, numSamples, seed, timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS, null);
	}
	
	public HASCOViaFDAndBestFirstWithRandomCompletions(RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, int numSamples, int seed, int timeoutForSingleCompletionEvaluationInMS, int timeoutForNodeEvaluationInMS, INodeEvaluator<TFDNode, V> preferredNodeEvaluator) {
		super(configurationProblem, new GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS<>(preferredNodeEvaluator, new DefaultPathPriorizingNodeEvaluator<>(), seed, numSamples, timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS));
	}
}
