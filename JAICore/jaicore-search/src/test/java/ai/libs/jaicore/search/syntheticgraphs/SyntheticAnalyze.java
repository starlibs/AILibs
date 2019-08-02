package ai.libs.jaicore.search.syntheticgraphs;

import java.util.Random;
import java.util.stream.Collectors;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.noisymean.LinkedTreasureIslandPathCostGenerator;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.noisymean.ShiftedSineTreasureGenerator;

public class SyntheticAnalyze {
	public static void main(final String[] args) {
		int branchingFactor = 5;
		int depth = 10;
		int distanceToIslands = 5;
		int numberOfTreasures = 1;
		int deadEndsPerLevel = 2;
		int seed = 2;

		int numberOfIslands = (int)Math.pow(branchingFactor, distanceToIslands);

		IIslandModel islandModel = new EqualSizedIslandsModel(10);
		ShiftedSineTreasureGenerator linkFuction = new ShiftedSineTreasureGenerator(numberOfIslands, numberOfTreasures, 0.1, 0.5);
		LinkedTreasureIslandPathCostGenerator treasureGenerator = new LinkedTreasureIslandPathCostGenerator(islandModel, linkFuction);
		//		ITreasureModel treasureGenerator = new ChaoticMeansTreasureModel(numberOfTreasures, islandModel, 0);


		ISyntheticTreasureIslandProblem problem = new DegeneratedGraphSearchWithPathEvaluationsProblem(new Random(seed), deadEndsPerLevel, branchingFactor, depth, treasureGenerator);


		/* create result string */
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(SyntheticGraphUtil.getScoresWithDepths(problem).stream().map(s -> s.getX() + ", " + s.getY()).collect(Collectors.joining("\n\t")));
		sb.append("]");

		System.out.println(sb.toString());
	}
}
