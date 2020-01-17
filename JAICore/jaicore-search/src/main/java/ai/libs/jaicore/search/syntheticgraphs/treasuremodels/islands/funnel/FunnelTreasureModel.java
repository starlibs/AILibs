package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.funnel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.math.linearalgebra.AffineFunction;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.AIslandTreasureModel;

public class FunnelTreasureModel extends AIslandTreasureModel {

	private final int numberOfTreasureIslands;
	private final Set<BigInteger> indicesOfIslands = new HashSet<>();
	private final int seed;

	private final double plateauMinForTreasures;
	private final double plateauMaxForTreasures;
	private final double plateauMinForNonTreasures;
	private final double plateauMaxForNonTreasures;
	private final double plateauSizes; // portion of the island that is plateau
	private final double funnelDepth;

	private double minimumAchievable = Double.MAX_VALUE;

	private final Map<BigInteger, Double> plateausOfIslands = new HashMap<>();

	public FunnelTreasureModel(final IIslandModel islandModel, final int numberOfTreasureIslands, final int seed, final double plateauMinForTreasures, final double plateauMaxForTreasures, final double plateauMinForNonTreasures, final double plateauMaxForNonTreasures,
			final double plateauSizes, final double funnelDepth) {
		super(islandModel);
		this.numberOfTreasureIslands = numberOfTreasureIslands;
		this.seed = seed;
		this.plateauMinForTreasures = plateauMinForTreasures;
		this.plateauMaxForTreasures = plateauMaxForTreasures;
		this.plateauMinForNonTreasures = plateauMinForNonTreasures;
		this.plateauMaxForNonTreasures = plateauMaxForNonTreasures;
		this.plateauSizes = plateauSizes;
		this.funnelDepth = funnelDepth;
	}

	public FunnelTreasureModel(final IIslandModel islandModel, final int numberOfTreasureIslands, final Random random) {
		this(islandModel, numberOfTreasureIslands, random.nextInt(), 0.1, 0.15, 0.7, 0.95, 0.5, .1);
	}

	private void distributeTreasures() {
		Random random = new Random(this.seed);
		while (this.indicesOfIslands.size() < this.numberOfTreasureIslands) {
			BigInteger newTreasureIsland;
			do {
				newTreasureIsland = new BigInteger(this.getIslandModel().getNumberOfIslands().bitLength(), random);
			}
			while (newTreasureIsland.compareTo(this.getIslandModel().getNumberOfIslands()) >= 0);
			this.indicesOfIslands.add(newTreasureIsland);
		}

		for (BigInteger island : this.indicesOfIslands) {
			double plateauOfThisIsland = this.plateauMinForTreasures + (this.plateauMaxForTreasures - this.plateauMinForTreasures) * random.nextDouble();
			this.plateausOfIslands.put(island, plateauOfThisIsland);
			this.minimumAchievable = Math.min(this.minimumAchievable, plateauOfThisIsland - this.funnelDepth);
		}
	}

	@Override
	public Double evaluate(final ILabeledPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException {
		if (this.indicesOfIslands.isEmpty()) {
			this.getIslandModel().setRootNode(path.getRoot());
			this.distributeTreasures();
		}
		BigInteger positionOnIsland = this.getIslandModel().getPositionOnIsland(path).add(BigInteger.ONE);
		BigInteger island = this.getIslandModel().getIsland(path);
		if (!this.plateausOfIslands.containsKey(island)) {
			this.plateausOfIslands.put(island, this.plateauMinForNonTreasures + (this.plateauMaxForNonTreasures - this.plateauMinForNonTreasures) * new Random(path.hashCode() + (long)this.seed).nextDouble());
		}
		double plateauOfIsland = this.plateausOfIslands.get(island);

		/* compute important island positions for distribution */
		BigInteger islandSize = this.getIslandModel().getSizeOfIsland(path);
		if (positionOnIsland.compareTo(islandSize) > 0) {
			throw new IllegalStateException("Position on island cannot be greater than the island itself.");
		}
		BigDecimal islandSizeAsDecimal = new BigDecimal(islandSize);
		BigDecimal c1;
		BigDecimal c2;
		BigDecimal median;
		if (islandSize.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
			c1 = islandSizeAsDecimal.multiply(BigDecimal.valueOf(this.plateauSizes / 2.0)).round(new MathContext(1, RoundingMode.CEILING));
			c2 = islandSizeAsDecimal.subtract(c1).round(new MathContext(1, RoundingMode.FLOOR));
			median = islandSizeAsDecimal.divide(BigDecimal.valueOf(2));
		}
		else {
			c1 = islandSizeAsDecimal.multiply(BigDecimal.valueOf(this.plateauSizes / 2.0)).round(new MathContext(1, RoundingMode.FLOOR));
			c2 = islandSizeAsDecimal.subtract(islandSizeAsDecimal.multiply(BigDecimal.valueOf(this.plateauSizes / 2.0))).round(new MathContext(1, RoundingMode.CEILING)).add(BigDecimal.ONE);
			median = islandSizeAsDecimal.add(BigDecimal.ONE).divide(BigDecimal.valueOf(2));
		}

		double val;
		if (positionOnIsland.compareTo(c1.toBigInteger()) <= 0 || positionOnIsland.compareTo(c2.toBigInteger()) > 0) {
			val = plateauOfIsland;
		}
		else if (positionOnIsland.compareTo(median.toBigInteger()) <= 0) {
			val = new AffineFunction(c1, BigDecimal.valueOf(plateauOfIsland), median, BigDecimal.valueOf(plateauOfIsland).subtract(BigDecimal.valueOf(this.funnelDepth))).applyAsDouble(positionOnIsland);
		}
		else {
			val = new AffineFunction(c2, BigDecimal.valueOf(plateauOfIsland), median, BigDecimal.valueOf(plateauOfIsland).subtract(BigDecimal.valueOf(this.funnelDepth))).applyAsDouble(positionOnIsland);
		}
		return val;
	}

	@Override
	public double getMinimumAchievable() {
		return this.minimumAchievable;
	}
}
