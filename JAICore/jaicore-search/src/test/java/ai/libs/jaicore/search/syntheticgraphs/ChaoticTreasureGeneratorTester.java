package ai.libs.jaicore.search.syntheticgraphs;

import org.junit.Test;

import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.ChaoticMeansTreasureModel;

public class ChaoticTreasureGeneratorTester {

	@Test
	public void test() {
		int numberOfTreasures = 1;
		int islandSize = 10;
		IIslandModel islandModel = new EqualSizedIslandsModel(islandSize);

		ITreasureModel treasureGenerator = new ChaoticMeansTreasureModel(numberOfTreasures, islandModel, 0);
		System.out.println(islandModel.getNumberOfIslands());
	}
}
