package ai.libs.jaicore.search.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.algorithm.IAlgorithm;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.basic.algorithm.IAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.algorithm.SolutionCandidateIteratorTester;
import ai.libs.jaicore.search.algorithms.mcts.enhancedttsp.EnhancedTTSPAsGraphSearchSet;
import ai.libs.jaicore.search.algorithms.mcts.knapsack.KnapsackProblemAsGraphSearchSet;
import ai.libs.jaicore.search.algorithms.mcts.nqueens.NQueensProblemAsGraphSearchSet;
import ai.libs.jaicore.search.algorithms.mcts.samegame.SameGameGraphSearchSet;

public abstract class GraphSearchSolutionIteratorTester extends SolutionCandidateIteratorTester {

	@Override
	public final IAlgorithm<?, ?> getAlgorithm(final Object problem) {
		return this.getSearchAlgorithm((IPathSearchInput<?, ?>) problem);
	}

	public IPathSearchInput<?, ?> getSimpleGraphSearchProblemInstance(final IAlgorithmTestProblemSet<?> problemSet) throws AlgorithmTestProblemSetCreationException, InterruptedException {
		return (IPathSearchInput<?, ?>)problemSet.getSimpleProblemInputForGeneralTestPurposes();
	}

	public IPathSearchInput<?, ?> getDifficultGraphSearchProblemInstance(final IAlgorithmTestProblemSet<?> problemSet) throws AlgorithmTestProblemSetCreationException, InterruptedException {
		return (IPathSearchInput<?, ?>)problemSet.getDifficultProblemInputForGeneralTestPurposes();
	}

	public abstract <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(IPathSearchInput<N, A> problem);

	public static Stream<Arguments> getProblemSets() {
		List<Arguments> problemSets = new ArrayList<>();
		problemSets.add(Arguments.of(new NQueensProblemAsGraphSearchSet()));
		problemSets.add(Arguments.of(new KnapsackProblemAsGraphSearchSet()));
		problemSets.add(Arguments.of(new EnhancedTTSPAsGraphSearchSet()));
		problemSets.add(Arguments.of(new SameGameGraphSearchSet()));
		return problemSets.stream();
	}
}
