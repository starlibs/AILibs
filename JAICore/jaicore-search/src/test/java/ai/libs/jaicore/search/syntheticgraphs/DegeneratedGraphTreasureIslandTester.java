package ai.libs.jaicore.search.syntheticgraphs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.ChaoticMeansTreasureModel;

public class DegeneratedGraphTreasureIslandTester extends SyntheticGraphTester {

	public static Stream<Arguments> getTreeSetups() {

		final int MAX_BF = 8;
		final int MAX_DEPTH = 4;
		final int MAX_ISLANDSIZE = 10;

		List<Arguments> data = new ArrayList<>();
		for (int bf = 2; bf <= MAX_BF; bf += 2) {
			for (int depth = 1; depth <= MAX_DEPTH; depth++) {
				for (int islandSize = 1; islandSize <= MAX_ISLANDSIZE; islandSize++) {
					data.add(Arguments.of(bf, depth, (int) Math.min(Math.pow(bf / 2, depth), islandSize),  1));
				}
			}
		}
		return data.stream();
	}

	@ParameterizedTest(name = "branchingFactor = {0}, depth = {1}, maxislandsize = {2}, numberOfIslandsWithTreasure = {3}")
	@MethodSource("getTreeSetups")
	public void testProblem(final int branchingFactor, final int depth, final int maxIslandSize, final int numberOfIslandsWithTreasure) throws PathEvaluationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		IIslandModel islandModel = new EqualSizedIslandsModel(maxIslandSize);
		ITreasureModel treasureModel = new ChaoticMeansTreasureModel(numberOfIslandsWithTreasure, islandModel, 0);
		this.testIslandModel(new DegeneratedGraphSearchWithPathEvaluationsProblem(new Random(0), branchingFactor / 2, branchingFactor, depth, maxIslandSize, numberOfIslandsWithTreasure, islandModel, treasureModel));
	}
}
