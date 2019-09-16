package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.funnel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.math.linearalgebra.AffineFunction;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.AIslandTreasureModel;

public class RelativeFunnelTreasureModel extends AIslandTreasureModel {

	private final int numberOfTreasureIslands;
	private final Set<BigInteger> indicesOfIslands = new HashSet<>();
	private final int seed;

	private final double plateauMinForTreasures = .8;
	private final double plateauMaxForTreasures = 1;
	private final double plateauMinForNonTreasures = .5;
	private final double plateauMaxForNonTreasures = .8;
	private final double plateauSizes = 0.8; // portion of the island that is plateau

	private double minimumAchievable = Double.MAX_VALUE;

	private final Map<BigInteger, Double> plateausOfIslands = new HashMap<>();

	public double getDepthOfFunnel(final double plateau) {
		return 6.29 * Math.pow(plateau, 1.25) - 5.29 * plateau;
	}

	public double getMinimumOfFunnel(final double plateau) {
		return plateau - this.getDepthOfFunnel(plateau);
	}

	public RelativeFunnelTreasureModel(final IIslandModel islandModel, final int numberOfTreasureIslands, final Random random) {
		super(islandModel);
		this.numberOfTreasureIslands = numberOfTreasureIslands;
		this.seed = random.nextInt();
	}

	private void distributeTreasures() {
		Random random = new Random(this.seed);
		while (this.indicesOfIslands.size() < this.numberOfTreasureIslands) {
			BigInteger newTreasureIsland;
			do {
				newTreasureIsland = new BigInteger(this.getIslandModel().getNumberOfIslands().bitLength(), random);
			} while (newTreasureIsland.compareTo(this.getIslandModel().getNumberOfIslands()) >= 0);
			this.indicesOfIslands.add(newTreasureIsland);
		}

		for (BigInteger island : this.indicesOfIslands) {
			double plateauOfThisIsland = this.plateauMinForTreasures + (this.plateauMaxForTreasures - this.plateauMinForTreasures) * random.nextDouble();
			this.plateausOfIslands.put(island, plateauOfThisIsland);
			this.minimumAchievable = Math.min(this.minimumAchievable, this.getMinimumOfFunnel(plateauOfThisIsland));
		}
		System.out.println("Treasures start");
		this.plateausOfIslands.forEach((i, s) -> System.out.println("\t" + i + ": " + s + ". Minimal value here: " + this.getMinimumOfFunnel(s)));
		System.out.println("Treasures end");
	}

	@Override
	public Double evaluate(final IPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException {
		if (this.indicesOfIslands.isEmpty()) {
			this.getIslandModel().setRootNode(path.getRoot());
			this.distributeTreasures();
		}
		BigInteger positionOnIsland = this.getIslandModel().getPositionOnIsland(path).add(BigInteger.ONE);
		BigInteger island = this.getIslandModel().getIsland(path);
		if (!this.plateausOfIslands.containsKey(island)) {
			this.plateausOfIslands.put(island, this.plateauMinForNonTreasures + (this.plateauMaxForNonTreasures - this.plateauMinForNonTreasures) * new Random(path.hashCode() * (long)this.seed).nextDouble());
			// System.out.println("\t" + island + ": " + this.plateausOfIslands.get(island));
		}
		double plateauOfIsland = this.plateausOfIslands.get(island);

		/* compute important island positions for distribution */
		BigInteger islandSize = this.getIslandModel().getSizeOfIsland(path);
		if (positionOnIsland.compareTo(islandSize) > 0) {
			throw new IllegalStateException("Position on island cannot be greater than the island itself.");
		}
		BigDecimal islandSizeAsDecimal = new BigDecimal(islandSize);
		BigDecimal abyssSegment = islandSizeAsDecimal.multiply(BigDecimal.valueOf((1 - this.plateauSizes) / 2));
		BigDecimal c1;
		BigDecimal c2;
		BigDecimal median;
		c1 = islandSizeAsDecimal.multiply(BigDecimal.valueOf(this.plateauSizes / 2.0));
		median = c1.add(abyssSegment);
		c2 = median.add(abyssSegment);
		BigDecimal minimumOfFunnel = BigDecimal.valueOf(this.getMinimumOfFunnel(plateauOfIsland));

		/* now compute value for current position */
		double val;
		if (positionOnIsland.compareTo(c1.toBigInteger()) <= 0 || positionOnIsland.compareTo(c2.toBigInteger()) > 0) {
			val = plateauOfIsland;
		} else if (positionOnIsland.compareTo(median.toBigInteger()) <= 0) {
			val = new AffineFunction(c1, BigDecimal.valueOf(plateauOfIsland), median, minimumOfFunnel).apply(positionOnIsland);
		} else {
			val = new AffineFunction(c2, BigDecimal.valueOf(plateauOfIsland), median, minimumOfFunnel).apply(positionOnIsland);
		}
		return val;
	}

	@Override
	public double getMinimumAchievable() {
		return this.minimumAchievable;
	}
}
