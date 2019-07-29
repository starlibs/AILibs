package ai.libs.jaicore.search.syntheticgraphs;

import java.util.stream.Collectors;

public class SyntheticAnalyze {
	public static void main(final String[] args) {
		int branchingFactor = 2;
		int depth = 10;
		int distanceToIslands = 5;
		int numberOfTreasures = 1;
		int seed = 2;

		int numberOfIslands = (int)Math.pow(branchingFactor, distanceToIslands);

		ShiftedSineTreasureGenerator linkFuction = new ShiftedSineTreasureGenerator(numberOfIslands, numberOfTreasures, 0.1, 0.5);
		LinkedTreasureIslandPathCostGenerator treasureGenerator = new LinkedTreasureIslandPathCostGenerator(numberOfTreasures, distanceToIslands, numberOfIslands, linkFuction);

		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(SyntheticGraphUtil.getScores(branchingFactor, depth, treasureGenerator).stream().map(Object::toString).collect(Collectors.joining("\n\t")));
		sb.append("]");
		System.out.println(sb.toString());
	}
}
