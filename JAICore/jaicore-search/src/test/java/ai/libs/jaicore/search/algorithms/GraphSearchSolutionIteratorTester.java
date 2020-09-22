package ai.libs.jaicore.search.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.algorithm.IAlgorithm;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
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

	public IPathSearchInput<?, ?> getSimpleGraphSearchProblemInstance() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		return (IPathSearchInput<?, ?>)this.getProblemSet().getSimpleProblemInputForGeneralTestPurposes();
	}

	public IPathSearchInput<?, ?> getDifficultGraphSearchProblemInstance() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		return (IPathSearchInput<?, ?>)this.getProblemSet().getDifficultProblemInputForGeneralTestPurposes();
	}

	public abstract <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(IPathSearchInput<N, A> problem);

	// creates the test data
	@Parameters(name = "problemset = {0}")
	public static Collection<Object[]> data() {
		List<Object> problemSets = new ArrayList<>();

		/* add N-Queens (as a graph search problem set) */
		problemSets.add(new NQueensProblemAsGraphSearchSet());
		problemSets.add(new KnapsackProblemAsGraphSearchSet());
		problemSets.add(new EnhancedTTSPAsGraphSearchSet());
		problemSets.add(new SameGameGraphSearchSet());
		List<Collection<Object>> input = new ArrayList<>();
		input.add(problemSets);

		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}
}
