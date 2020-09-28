package ai.libs.jaicore.search.syntheticgraphs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced.BalancedGraphSearchWithPathEvaluationsProblem;

public class BalancedTreasureIslandTester extends SyntheticGraphTester {

	public static Stream<Arguments> getTreeSetups() {
		final int MAX_BF = 4;
		final int MAX_DEPTH = 4;
		List<Arguments> data = new ArrayList<>();
		for (int bf = 1; bf <= MAX_BF; bf++) {
			for (int depth = 1; depth <= MAX_DEPTH; depth++) {
				int x = Math.min(3, depth);
				data.add(Arguments.of(bf, depth, x, Math.min(2, (int) Math.pow(bf, x - 1))));
			}
		}
		return data.stream();
	}

	@ParameterizedTest(name = "branchingFactor = {0}, depth = {1}, distanceToIslands = {2}, numberOfIslandsWithTreasure = {3}")
	@MethodSource("getTreeSetups")
	public void testProblem(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure) throws PathEvaluationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		this.testIslandModel(new BalancedGraphSearchWithPathEvaluationsProblem(branchingFactor, depth, distanceToIslands, numberOfIslandsWithTreasure));
	}
}
