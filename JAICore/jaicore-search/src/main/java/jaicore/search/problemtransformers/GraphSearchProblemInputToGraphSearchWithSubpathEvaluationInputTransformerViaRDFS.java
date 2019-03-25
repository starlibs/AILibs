package jaicore.search.problemtransformers;

import java.util.Random;
import java.util.function.Predicate;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<N, A, V extends Comparable<V>> extends GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<N, A, V> {

	private final INodeEvaluator<N, V> preferredNodeEvaluator;
	private final Predicate<N> prioritizedNodesInRandomCompletion;
	private final int seed;
	private final int numSamples;
	private final int timeoutForSingleCompletionEvaluationInMS;
	private final int timeoutForNodeEvaluationInMS;

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS(final INodeEvaluator<N, V> preferredNodeEvaluator, final Predicate<N> preferredNodeEvaluatorForRandomCompletion, final int seed,
			final int numSamples, final int timeoutForSingleCompletionEvaluationInMS, final int timeoutForNodeEvaluationInMS) {
		super();
		if (numSamples <= 0) {
			throw new IllegalArgumentException("Sample size has been set to " + numSamples + " but must be strictly positive!");
		}
		this.preferredNodeEvaluator = preferredNodeEvaluator;
		this.prioritizedNodesInRandomCompletion = preferredNodeEvaluatorForRandomCompletion;
		this.seed = seed;
		this.numSamples = numSamples;
		this.timeoutForSingleCompletionEvaluationInMS = timeoutForSingleCompletionEvaluationInMS;
		this.timeoutForNodeEvaluationInMS = timeoutForNodeEvaluationInMS;
	}

	public INodeEvaluator<N, V> getPreferredNodeEvaluator() {
		return this.preferredNodeEvaluator;
	}

	public Predicate<N> getPrioritizedNodePredicatesForRandomCompletion() {
		return this.prioritizedNodesInRandomCompletion;
	}

	public int getSeed() {
		return this.seed;
	}

	public int getNumSamples() {
		return this.numSamples;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, V> encodeProblem(final GraphSearchWithPathEvaluationsInput<N, A, V> problem) {
		RandomCompletionBasedNodeEvaluator<N, A, V> rdfsNodeEvaluator = new RandomCompletionBasedNodeEvaluator<>(new Random(this.seed), this.numSamples, problem.getPathEvaluator(), this.timeoutForSingleCompletionEvaluationInMS,
				this.timeoutForNodeEvaluationInMS, this.prioritizedNodesInRandomCompletion);
		if (this.preferredNodeEvaluator != null) {
			this.setNodeEvaluator(new AlternativeNodeEvaluator<>(this.preferredNodeEvaluator, rdfsNodeEvaluator));
		} else {
			this.setNodeEvaluator(rdfsNodeEvaluator);
		}
		return super.encodeProblem(problem);
	}

}
