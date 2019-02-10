package jaicore.search.problemtransformers;

import java.util.Random;
import java.util.function.Predicate;

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

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS(INodeEvaluator<N, V> preferredNodeEvaluator, Predicate<N> preferredNodeEvaluatorForRandomCompletion, int seed, int numSamples, int timeoutForSingleCompletionEvaluationInMS, int timeoutForNodeEvaluationInMS) {
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
		return preferredNodeEvaluator;
	}

	public Predicate<N> getPrioritizedNodePredicatesForRandomCompletion() {
		return prioritizedNodesInRandomCompletion;
	}

	public int getSeed() {
		return seed;
	}

	public int getNumSamples() {
		return numSamples;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, V> transform(GraphSearchWithPathEvaluationsInput<N, A, V> problem) {
		setNodeEvaluator(new RandomCompletionBasedNodeEvaluator<>(new Random(seed), numSamples, problem.getPathEvaluator(), timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS, prioritizedNodesInRandomCompletion));
		return super.transform(problem);
	}

}
