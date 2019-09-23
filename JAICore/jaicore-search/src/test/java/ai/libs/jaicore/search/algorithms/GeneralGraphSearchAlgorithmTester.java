package ai.libs.jaicore.search.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.IGraphSearch;
import org.api4.java.algorithm.IAlgorithm;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.search.exampleproblemtesters.EnhancedTTSPAsGraphSearchSet;
import ai.libs.jaicore.search.exampleproblemtesters.KnapsackProblemAsGraphSearchSet;
import ai.libs.jaicore.search.exampleproblemtesters.NQueensProblemAsGraphSearchSet;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public abstract class GeneralGraphSearchAlgorithmTester extends GeneralAlgorithmTester {

	@Override
	public final IAlgorithm<?, ?> getAlgorithm(final Object problem) {
		return this.getSearchAlgorithm((GraphSearchInput<?, ?>) problem);
	}

	public abstract <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(GraphSearchInput<N, A> problem);

	// creates the test data
	@Parameters(name = "problemset = {0}")
	public static Collection<Object[]> data() {
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
