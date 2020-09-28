package ai.libs.jaicore.search.syntheticgraphs;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.funnel.FunnelTreasureModel;

public class FunnelTreasureIslandTester extends SyntheticGraphTester {

	@Test
	public void test() throws PathEvaluationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		int branchingFactor = 2;
		int depth = 10;
		int numberOfTreasures = 1;
		int islandSize = 10;
		IIslandModel islandModel = new EqualSizedIslandsModel(islandSize);
		ITreasureModel treasureGenerator = new FunnelTreasureModel(islandModel, numberOfTreasures, new Random(0));

		this.testIslandModel(new DegeneratedGraphSearchWithPathEvaluationsProblem(new Random(0), branchingFactor / 2, branchingFactor, depth, islandSize, numberOfTreasures, islandModel, treasureGenerator));

	}
}
