package ai.libs.jaicore.search.syntheticgraphs;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.noisymean.ChaoticMeansTreasureModel;

public class SyntheticAnalyze {
	public static void main(final String[] args) {
		int branchingFactor = 5;
		int depth = 8;
		int numberOfTreasures = 4;
		int deadEndsPerLevel = 2;
		int seed = 2;

		int islandSize = 8000;

		IIslandModel islandModel = new EqualSizedIslandsModel(islandSize);
		//				ShiftedSineTreasureGenerator linkFuction = new ShiftedSineTreasureGenerator(islandModel, numberOfTreasures, 0.1, 0.5);
		//				LinkedTreasureIslandPathCostGenerator treasureGenerator = new LinkedTreasureIslandPathCostGenerator(islandModel, linkFuction);
		ITreasureModel treasureGenerator = new ChaoticMeansTreasureModel(numberOfTreasures, islandModel, seed);


		ISyntheticTreasureIslandProblem problem = new DegeneratedGraphSearchWithPathEvaluationsProblem(new Random(seed), deadEndsPerLevel, branchingFactor, depth, treasureGenerator);
		List<Pair<Integer,Double>> scores = SyntheticGraphUtil.getScoresWithDepths(problem);

		//		if (islandModel.getNumberOfIslands() != Math.ceil(scores.size() / (islandSize * 1.0))) {
		//			throw new IllegalStateException("Expected: " + islandModel.getNumberOfIslands() + ". Seen: " + scores.size());
		//		}

		if (islandModel.getNumberOfIslands() < numberOfTreasures) {
			throw new IllegalArgumentException("Number of treasures cannot be higher than number of islands!");
		}
		System.out.println(islandModel.getNumberOfIslands());

		/* create result string */
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(scores.stream().map(s -> s.getX() + ", " + s.getY()).collect(Collectors.joining("\n\t")));
		sb.append("]");

		System.out.println(sb.toString());
	}
}
