package jaicore.search.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.runners.Parameterized.Parameters;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPAsGraphSearchSet;
import jaicore.search.testproblems.knapsack.KnapsackProblemAsGraphSearchSet;
import jaicore.search.testproblems.nqueens.NQueensProblemAsGraphSearchSet;

public abstract class GraphSearchWithSubPathEvaluationUninformedTester extends GraphSearchTester {

	@Override
	public final <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchInput<N, A> problem) {
		GraphSearchWithSubpathEvaluationsInput<N, A, Double> transformed = new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), n -> 0.0);
		return getSearchAlgorithm(transformed);
	}
	
	public abstract <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchWithSubpathEvaluationsInput<N, A, Double> problem);

	// creates the test data
	@Parameters(name = "problemset = {0}")
	public static Collection<Object[]> data() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		List<Object> problemSets = new ArrayList<>();

		/* add N-Queens (as a graph search problem set) */
		problemSets.add(new NQueensProblemAsGraphSearchSet());
		problemSets.add(new KnapsackProblemAsGraphSearchSet());
		problemSets.add(new EnhancedTTSPAsGraphSearchSet());
		List<Collection<Object>> input = new ArrayList<>();
		input.add(problemSets);

		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}
}
