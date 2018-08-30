package jaicore.search.problemtransformers;

import java.util.Random;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;

public class GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS<N, A, V extends Comparable<V>>
		implements AlgorithmProblemTransformer<GraphSearchProblemInput<N, A, V>, GeneralEvaluatedTraversalTree<N, A, V>> {

	private final INodeEvaluator<N, V> preferredNodeEvaluator;
	private final int seed;
	private final int numSamples;

	public GraphSearchProblemInputToGeneralEvaluatedTraversalTreeViaRDFS(INodeEvaluator<N, V> preferredNodeEvaluator, int seed, int numSamples) {
		super();
		this.preferredNodeEvaluator = preferredNodeEvaluator;
		this.seed = seed;
		this.numSamples = numSamples;
	}

	public INodeEvaluator<N, V> getPreferredNodeEvaluator() {
		return preferredNodeEvaluator;
	}

	public int getSeed() {
		return seed;
	}

	public int getNumSamples() {
		return numSamples;
	}

	@Override
	public GeneralEvaluatedTraversalTree<N, A, V> transform(GraphSearchProblemInput<N, A, V> problem) {

		return new GeneralEvaluatedTraversalTree<>(problem.getGraphGenerator(), new RandomCompletionBasedNodeEvaluator<>(new Random(seed), numSamples, null, problem.getPathEvaluator()));
	}

}
