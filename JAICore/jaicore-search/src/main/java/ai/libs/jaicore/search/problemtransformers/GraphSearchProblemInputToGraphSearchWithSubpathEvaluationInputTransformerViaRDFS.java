package ai.libs.jaicore.search.problemtransformers;

import java.util.Random;
import java.util.function.Predicate;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.CoveringNodeEvaluator;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<N, A, V extends Comparable<V>> extends GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<N, A, V> {

	private final IPathEvaluator<N, A, V> preferredNodeEvaluator; // this is a path evaluator that is executed PRIOR to the random completion
	private final Predicate<N> prioritizedNodesInRandomCompletion; // the predicate passed to the RandomCompletionNodeEvaluator for preferred paths
	private IPathEvaluator<N, A, V> coveringNodeEvaluator; // this is a path evaluator that is executed AFTER the random completion (and only if the preferred NE returned NULL), and its result is actually returned. If not NULL, the random
	// completion is only used to probe solutions and propagate their score via the event bus.
	private final long seed;
	private final int numSamples;
	private final int timeoutForSingleCompletionEvaluationInMS;
	private final int timeoutForNodeEvaluationInMS;

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS(final IPathEvaluator<N, A, V> preferredNodeEvaluator, final Predicate<N> preferredNodeEvaluatorForRandomCompletion, final long seed,
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

	public IPathEvaluator<N, A, V> getPreferredNodeEvaluator() {
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
		IPathEvaluator<N, A, V> mainEvaluator;
		RandomCompletionBasedNodeEvaluator<N, A, V> rdfsNodeEvaluator = new RandomCompletionBasedNodeEvaluator<>(new Random(this.seed), this.numSamples, this.numSamples * 2, problem.getPathEvaluator(),
				this.timeoutForSingleCompletionEvaluationInMS, this.timeoutForNodeEvaluationInMS, this.prioritizedNodesInRandomCompletion);

		/* first check whether we have a covering node evaluator */
		if (this.coveringNodeEvaluator != null) {
			mainEvaluator = new CoveringNodeEvaluator<>(rdfsNodeEvaluator, this.coveringNodeEvaluator);
		} else {
			mainEvaluator = rdfsNodeEvaluator;
		}

		/* now merge this main evaluator together with the preferred node evaluator */
		if (this.preferredNodeEvaluator != null) {
			this.setNodeEvaluator(new AlternativeNodeEvaluator<>(this.preferredNodeEvaluator, mainEvaluator));
		} else {
			this.setNodeEvaluator(mainEvaluator);
		}
		return super.encodeProblem(problem);
	}

	public IPathEvaluator<N, A, V> getCoveringNodeEvaluator() {
		return this.coveringNodeEvaluator;
	}

	public void setCoveringNodeEvaluator(final IPathEvaluator<N, A, V> coveringNodeEvaluator) {
		this.coveringNodeEvaluator = coveringNodeEvaluator;
	}
}
