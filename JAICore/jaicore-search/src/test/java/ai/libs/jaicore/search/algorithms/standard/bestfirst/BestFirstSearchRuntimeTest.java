package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.problems.nqueens.NQueensProblem;
import ai.libs.jaicore.search.algorithms.standard.dfs.TinyDepthFirstSearch;
import ai.libs.jaicore.search.exampleproblems.nqueens.NQueensToGraphSearchReducer;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness;

@RunWith(Parameterized.class)
public class BestFirstSearchRuntimeTest {

	private static final Logger logger = LoggerFactory.getLogger("testers");

	@Parameters(name = "problem = {0}")
	public static Collection<Object[]> data() {
		List<Object> problemSets = new ArrayList<>();
		NQueensToGraphSearchReducer nQueensReducer = new NQueensToGraphSearchReducer();
		for (int i = 2; i <= 10; i++) {
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
		assertTrue("Only achieved " + expansionsPerSecond + " but 1000 were required. Total runtime was " + runtime + " for " + bf.getExpandedCounter() + " expansions.", runtime < 1000 || (expansionsPerSecond > 1000));
		assertTrue(runtime < 1000 || creationsPerSecond > 1000);
		logger.info("Needed {}ms to identify {} solutions. Expanded {}/{} created nodes. This corresponds to {} expansions and {} creations per second.", runtime, bf.getSolutionQueue().size(), bf.getExpandedCounter(), bf.getCreatedCounter(), expansionsPerSecond, creationsPerSecond);
	}

	@Test
	public void measureRuntimeForDFS() throws InterruptedException {
		TinyDepthFirstSearch<?, ?> dfs = new TinyDepthFirstSearch<>(this.problem);
		long start = System.currentTimeMillis();
		dfs.run();
		int runtime = (int) (System.currentTimeMillis() - start);
		assertTrue(true);
		logger.info("Needed {}ms to identify {} solutions.", runtime, dfs.getSolutionPaths().size());
	}
}
