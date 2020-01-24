package ai.libs.jaicore.search.problemtransformers;

import java.util.Random;
import java.util.function.Predicate;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<N, A, V extends Comparable<V>> extends GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<N, A, V> {

	private final IPathEvaluator<N, A,V> preferredNodeEvaluator;
	private final Predicate<N> prioritizedNodesInRandomCompletion;
	private final long seed;
	private final int numSamples;
	private final int timeoutForSingleCompletionEvaluationInMS;
	private final int timeoutForNodeEvaluationInMS;

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS(final IPathEvaluator<N, A,V> preferredNodeEvaluator, final Predicate<N> preferredNodeEvaluatorForRandomCompletion, final long seed,
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

	public IPathEvaluator<N, A,V> getPreferredNodeEvaluator() {
		return this.preferredNodeEvaluator;
	}

	public Predicate<N> getPrioritizedNodePredicatesForRandomCompletion() {
		return this.prioritizedNodesInRandomCompletion;
	}

	public long getSeed() {
		return this.seed;
	}

	public int getNumSamples() {
		return this.numSamples;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, V> encodeProblem(final IPathSearchWithPathEvaluationsInput<N, A, V> problem) {
		RandomCompletionBasedNodeEvaluator<N, A, V> rdfsNodeEvaluator = new RandomCompletionBasedNodeEvaluator<>(new Random(this.seed), this.numSamples, this.numSamples * 2, problem.getPathEvaluator(), this.timeoutForSingleCompletionEvaluationInMS,
				this.timeoutForNodeEvaluationInMS, this.prioritizedNodesInRandomCompletion);
		if (this.preferredNodeEvaluator != null) {
			this.setNodeEvaluator(new AlternativeNodeEvaluator<>(this.preferredNodeEvaluator, rdfsNodeEvaluator));
		} else {
			this.setNodeEvaluator(rdfsNodeEvaluator);
		}
		return super.encodeProblem(problem);
	}

}
