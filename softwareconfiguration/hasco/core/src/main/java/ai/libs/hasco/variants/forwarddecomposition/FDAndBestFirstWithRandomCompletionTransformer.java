package ai.libs.hasco.variants.forwarddecomposition;

import java.util.function.Predicate;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.IHascoAware;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;

public class FDAndBestFirstWithRandomCompletionTransformer<V extends Comparable<V>> extends GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<TFDNode, String, V> implements IHascoAware {

	private HASCO<?, ?, ?, ?> hasco;

	public FDAndBestFirstWithRandomCompletionTransformer(final IPathEvaluator<TFDNode, String, V> preferredNodeEvaluator, final Predicate<TFDNode> preferredNodeEvaluatorForRandomCompletion, final long seed, final int numSamples,
			final int timeoutForSingleCompletionEvaluationInMS, final int timeoutForNodeEvaluationInMS) {
		super(preferredNodeEvaluator, preferredNodeEvaluatorForRandomCompletion, seed, numSamples, timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS);
	}

	@Override
	public void setHascoReference(final HASCO hasco) {
		if (this.getPrioritizedNodePredicatesForRandomCompletion() instanceof IHascoAware) {
			((IHascoAware) this.getPrioritizedNodePredicatesForRandomCompletion()).setHascoReference(hasco);
		}
		this.hasco = hasco;
	}

	@Override
	public HASCO<?, ?, ?, ?> getHASCOReference() {
		return this.hasco;
	}
}
