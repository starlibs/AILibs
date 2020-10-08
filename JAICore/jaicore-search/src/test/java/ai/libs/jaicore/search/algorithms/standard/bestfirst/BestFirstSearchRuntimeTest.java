package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.problems.nqueens.NQueensProblem;
import ai.libs.jaicore.search.algorithms.standard.dfs.TinyDepthFirstSearch;
import ai.libs.jaicore.search.exampleproblems.nqueens.NQueensToGraphSearchReducer;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness;
import ai.libs.jaicore.test.MediumTest;

public class BestFirstSearchRuntimeTest extends ATest{

	public static Stream<Arguments> getProblemSets() {
		List<Arguments> problemSets = new ArrayList<>();
		NQueensToGraphSearchReducer nQueensReducer = new NQueensToGraphSearchReducer();
		for (int i = 2; i <= 10; i++) {
			problemSets.add(Arguments.of(nQueensReducer.encodeProblem(new NQueensProblem(i))));
		}
		return problemSets.stream();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@MediumTest
	@ParameterizedTest(name="Measure Runtime for BestFirst on {0}")
	@MethodSource("getProblemSets")
	public <N, A> void measureRuntime(final GraphSearchInput<?, ?> problem) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness transformer = new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness();
		GraphSearchWithSubpathEvaluationsInput<N, A, Double> reducedProblem = transformer.encodeProblem(problem);
		StandardBestFirst<N, A, Double> bf = new StandardBestFirst<>(reducedProblem);
		long start = System.currentTimeMillis();
		bf.call();
		int runtime = (int) (System.currentTimeMillis() - start);
		double expansionsPerSecond = MathExt.round(bf.getExpandedCounter() / (runtime / 1000f), 2);
		double creationsPerSecond = MathExt.round(bf.getCreatedCounter() / (runtime / 1000f), 2);
		assertTrue("Only achieved " + expansionsPerSecond + " but 900 were required. Total runtime was " + runtime + " for " + bf.getExpandedCounter() + " expansions.", runtime < 1000 || (expansionsPerSecond > 900));
		assertTrue(runtime < 1000 || creationsPerSecond > 1000);
		this.logger.info("Needed {}ms to identify {} solutions. Expanded {}/{} created nodes. This corresponds to {} expansions and {} creations per second.", runtime, bf.getSolutionQueue().size(), bf.getExpandedCounter(), bf.getCreatedCounter(), expansionsPerSecond, creationsPerSecond);
	}

	@MediumTest
	@ParameterizedTest(name="Measure Runtime for DFS on {0}")
	@MethodSource("getProblemSets")
	public void measureRuntimeForDFS(final GraphSearchInput<?, ?> problem) throws InterruptedException {
		TinyDepthFirstSearch<?, ?> dfs = new TinyDepthFirstSearch<>(problem);
		long start = System.currentTimeMillis();
		dfs.run();
		int runtime = (int) (System.currentTimeMillis() - start);
		assertTrue(true);
		this.logger.info("Needed {}ms to identify {} solutions.", runtime, dfs.getSolutionPaths().size());
	}
}
