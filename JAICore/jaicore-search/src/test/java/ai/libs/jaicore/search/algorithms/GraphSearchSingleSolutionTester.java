package ai.libs.jaicore.search.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.algorithm.IAlgorithm;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.jaicore.basic.algorithm.SolutionCandidateFinderTester;
import ai.libs.jaicore.search.algorithms.mcts.enhancedttsp.EnhancedTTSPAsGraphSearchSet;
import ai.libs.jaicore.search.algorithms.mcts.knapsack.KnapsackProblemAsGraphSearchSet;
import ai.libs.jaicore.search.algorithms.mcts.nqueens.NQueensProblemAsGraphSearchSet;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public abstract class GraphSearchSingleSolutionTester extends SolutionCandidateFinderTester {

	@Override
	public final IAlgorithm<?, ?> getAlgorithm(final Object problem) {
		return this.getSearchAlgorithm((GraphSearchInput<?, ?>) problem);
	}

	public abstract <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchInput<N, A> problem);

	public static Stream<Arguments> getProblemSets() {
		List<Arguments> problemSets = new ArrayList<>();
		problemSets.add(Arguments.of(new NQueensProblemAsGraphSearchSet()));
		problemSets.add(Arguments.of(new KnapsackProblemAsGraphSearchSet()));
		problemSets.add(Arguments.of(new EnhancedTTSPAsGraphSearchSet()));
		return problemSets.stream();
	}
}
