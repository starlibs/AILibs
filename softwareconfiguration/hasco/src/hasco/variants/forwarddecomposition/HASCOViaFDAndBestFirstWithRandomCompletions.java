package hasco.variants.forwarddecomposition;

import java.util.function.Predicate;

import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS;

public class HASCOViaFDAndBestFirstWithRandomCompletions<V extends Comparable<V>> extends HASCOViaFDAndBestFirst<V> {

	public HASCOViaFDAndBestFirstWithRandomCompletions(RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, int numSamples, int seed, int timeoutForSingleCompletionEvaluationInMS,
			int timeoutForNodeEvaluationInMS) {
		this(configurationProblem, null, numSamples, seed, timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS, null);
	}

	public HASCOViaFDAndBestFirstWithRandomCompletions(RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, Predicate<TFDNode> prioritingPredicate, int numSamples, int seed, int timeoutForSingleCompletionEvaluationInMS,
			int timeoutForNodeEvaluationInMS, INodeEvaluator<TFDNode, V> preferredNodeEvaluator) {
		super(configurationProblem, new GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS<>(preferredNodeEvaluator, prioritingPredicate, seed, numSamples,
				timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS));
	}
}
