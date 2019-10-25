package ai.libs.hasco.variants.forwarddecomposition;

import java.util.function.Predicate;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.StandardBestFirstFactory;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;

public class HASCOViaFDAndBestFirstWithRandomCompletions<V extends Comparable<V>> extends HASCOViaFDAndBestFirst<V> {

	public HASCOViaFDAndBestFirstWithRandomCompletions(final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, final int numSamples, final int seed, final int timeoutForSingleCompletionEvaluationInMS,
			final int timeoutForNodeEvaluationInMS) {
		this(configurationProblem, null, numSamples, seed, timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS, n -> null);
	}

	public HASCOViaFDAndBestFirstWithRandomCompletions(final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, final Predicate<TFDNode> prioritingPredicate, final int numSamples, final int seed, final int timeoutForSingleCompletionEvaluationInMS,
			final int timeoutForNodeEvaluationInMS, final IPathEvaluator<TFDNode, String, V> preferredNodeEvaluator) {
		super(configurationProblem, new StandardBestFirstFactory<>(), new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<TFDNode, String, V>(preferredNodeEvaluator, prioritingPredicate, seed, numSamples,
				timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS));
	}
}
