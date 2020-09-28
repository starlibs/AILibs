package ai.libs.jaicore.search.problemtransformers;

import java.util.Random;
import java.util.function.Predicate;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.CoveringNodeEvaluator;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

/**
 * Takes a path search problem and uses the path evaluator as the evaluator within the random completion based node evaluator.
 *
 * @author Felix Mohr
 *
 * @param <N> node type
 * @param <A> arc type
 * @param <V> type of node evaluations
 */
public class GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<N, A, V extends Comparable<V>> extends GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<N, A, V> {

	private final IPathEvaluator<N, A, V> preferredNodeEvaluator; // this is a path evaluator that is executed PRIOR to the random completion
	private final Predicate<N> prioritizedNodesInRandomCompletion; // the predicate passed to the RandomCompletionNodeEvaluator for preferred paths
	private IPathEvaluator<N, A, V> coveringNodeEvaluator; // this is a path evaluator that is executed AFTER the random completion (and only if the preferred NE returned NULL), and its result is actually returned. If not NULL, the random
	// completion is only used to probe solutions and propagate their score via the event bus.
	private final Random random;
	private final int numSamples;
	private final int timeoutForSingleCompletionEvaluationInMS;
	private final int timeoutForNodeEvaluationInMS;

	/**
	 *
	 * @param preferredNodeEvaluator Node evaluator that should be used prior to adopting random completions
	 * @param prioritizedNodesInRandomCompletion Predicate that evaluates to true for nodes that should be perferred when drawing random completions
	 * @param random Random source
	 * @param numSamples Number of random completions
	 * @param timeoutForSingleCompletionEvaluationInMS
	 * @param timeoutForNodeEvaluationInMS
	 */
	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS(final IPathEvaluator<N, A, V> preferredNodeEvaluator, final Predicate<N> prioritizedNodesInRandomCompletion, final Random random,
			final int numSamples, final int timeoutForSingleCompletionEvaluationInMS, final int timeoutForNodeEvaluationInMS) {
		super();
		if (numSamples <= 0) {
			throw new IllegalArgumentException("Sample size has been set to " + numSamples + " but must be strictly positive!");
		}
		this.preferredNodeEvaluator = preferredNodeEvaluator;
		this.prioritizedNodesInRandomCompletion = prioritizedNodesInRandomCompletion;
		this.random = random;
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

	public int getNumSamples() {
		return this.numSamples;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, V> encodeProblem(final IPathSearchInput<N, A> problem) {
		if (!(problem instanceof IPathSearchWithPathEvaluationsInput)) {
			throw new IllegalArgumentException("Given problem must have path evaluation!");
		}
		IPathSearchWithPathEvaluationsInput<N, A, V> cProblem = (IPathSearchWithPathEvaluationsInput<N, A, V>)problem;
		IPathEvaluator<N, A, V> mainEvaluator;
		RandomCompletionBasedNodeEvaluator<N, A, V> rdfsNodeEvaluator = new RandomCompletionBasedNodeEvaluator<>(this.random, this.numSamples, this.numSamples * 2, cProblem.getPathEvaluator(),
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
