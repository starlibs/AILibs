package ai.libs.jaicore.search.syntheticgraphs;

import java.util.Random;

import org.junit.Test;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.funnel.FunnelTreasureModel;

public class FunnelTreasureIslandTester {

	@Test
	public void test() {
		int branchingFactor = 2;
		int depth = 10;
		int numberOfTreasures = 1;
		int islandSize = 10;
		IIslandModel islandModel = new EqualSizedIslandsModel(islandSize);

		ITreasureModel treasureGenerator = new FunnelTreasureModel(islandModel, numberOfTreasures, new Random(0));
		DegeneratedGraphSearchWithPathEvaluationsProblem input = new DegeneratedGraphSearchWithPathEvaluationsProblem(new Random(0), branchingFactor / 2, branchingFactor, depth, treasureGenerator);


	}
}
