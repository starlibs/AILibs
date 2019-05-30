package jaicore.search.algorithms.standard.bestfirst;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import jaicore.basic.MathExt;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.search.algorithms.standard.dfs.TinyDepthFirstSearch;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness;
import jaicore.search.testproblems.nqueens.NQueensToGraphSearchReducer;
import jaicore.testproblems.nqueens.NQueensProblem;

@RunWith(Parameterized.class)
public class BestFirstSearchRuntimeTest {

	@Parameters(name = "problem = {0}")
	public static Collection<Object[]> data() {
		List<Object> problemSets = new ArrayList<>();
		NQueensToGraphSearchReducer nQueensReducer = new NQueensToGraphSearchReducer();
		for (int i = 4; i < 16; i++) {
			problemSets.add(nQueensReducer.encodeProblem(new NQueensProblem(i)));
		}

		/* turn the problem sets into an array */
		List<Collection<Object>> input = new ArrayList<>();
		input.add(problemSets);
		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}

	// fields used together with @Parameter must be public
	@Parameter(0)
	public GraphSearchInput<?, ?> problem;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public <N, A> void measureRuntime() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness transformer = new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness();
		GraphSearchWithSubpathEvaluationsInput<N, A, Double> reducedProblem = transformer.encodeProblem(this.problem);
		StandardBestFirst<N, A, Double> bf = new StandardBestFirst<>(reducedProblem);
		long start = System.currentTimeMillis();
		bf.call();
		int runtime = (int) (System.currentTimeMillis() - start);
		double expansionsPerSecond = MathExt.round(bf.getExpandedCounter() / (runtime / 1000f), 2);
		double creationsPerSecond = MathExt.round(bf.getCreatedCounter() / (runtime / 1000f), 2);
		assertTrue(expansionsPerSecond > 1000);
		assertTrue(creationsPerSecond > 1000);
		System.out.println("Needed " + runtime + "ms to identify " + bf.getSolutionQueue().size() + " solutions. Expanded " + bf.getExpandedCounter() + "/" + bf.getCreatedCounter() + " created nodes. This corresponds to "
				+ expansionsPerSecond + " expansions and " + creationsPerSecond + " creations per second.");
	}

	@Test
	public void measureRuntimeForDFS() throws InterruptedException {
		TinyDepthFirstSearch<?, ?> dfs = new TinyDepthFirstSearch<>(this.problem);
		long start = System.currentTimeMillis();
		dfs.run();
		int runtime = (int) (System.currentTimeMillis() - start);
		assertTrue(true);
		System.out.println("Needed " + runtime + "ms to identify " + dfs.getSolutionPaths().size() + " solutions.");
	}
}
