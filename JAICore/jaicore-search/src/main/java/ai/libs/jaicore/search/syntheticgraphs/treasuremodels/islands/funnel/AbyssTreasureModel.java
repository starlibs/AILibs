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

/**
 * the landscape is:
 *
 * plateau + mountain + abyss + mountain + plateau
 *
 * @author felix
 *
 */
public class AbyssTreasureModel extends AIslandTreasureModel {

	private final int numberOfTreasureIslands;
	private final Set<BigInteger> indicesOfIslands = new HashSet<>();
	private final int seed;

	private final double plateauMinForTreasures;
	private final double plateauMaxForTreasures;
	private final double plateauMinForNonTreasures;
	private final double plateauMaxForNonTreasures;
	private final double plateauWidths; // portion of the island that is plateau
	private final double moutainWidths;
	private final double mountainHeight;
	private final double abyssDepth;

	public AbyssTreasureModel(final IIslandModel islandModel, final int numberOfTreasureIslands, final int seed, final double plateauMinForTreasures, final double plateauMaxForTreasures, final double plateauMinForNonTreasures, final double plateauMaxForNonTreasures,
			final double plateauWidths, final double moutainWidths, final double mountainHeight, final double abyssDepth) {
		super(islandModel);
		this.numberOfTreasureIslands = numberOfTreasureIslands;
		this.seed = seed;
		this.plateauMinForTreasures = plateauMinForTreasures;
		this.plateauMaxForTreasures = plateauMaxForTreasures;
		this.plateauMinForNonTreasures = plateauMinForNonTreasures;
		this.plateauMaxForNonTreasures = plateauMaxForNonTreasures;
		this.plateauWidths = plateauWidths;
		this.moutainWidths = moutainWidths;
		this.mountainHeight = mountainHeight;
		this.abyssDepth = abyssDepth;
		if (this.moutainWidths + this.plateauWidths >= 1) {
			throw new IllegalArgumentException();
		}
	}

	private double minimumAchievable = Double.MAX_VALUE;

	private final Map<BigInteger, Double> plateausOfIslands = new HashMap<>();

	public AbyssTreasureModel(final IIslandModel islandModel, final int numberOfTreasureIslands, final Random random) {
		this(islandModel, numberOfTreasureIslands, random.nextInt(), 0.1, 0.15, .2, .8, .2, .4, .2, .1);

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
			this.minimumAchievable = Math.min(this.minimumAchievable, plateauOfThisIsland - this.abyssDepth);
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
		BigDecimal k1; // first kink
		BigDecimal p1; // first peak
		BigDecimal k2; // second kink
		BigDecimal k3; // third kink
		BigDecimal p2; // second peak
		BigDecimal k4; // fourth kink
		BigDecimal median;
		BigDecimal mountainSegment = islandSizeAsDecimal.multiply(BigDecimal.valueOf(this.moutainWidths / 4.0));
		BigDecimal abyssSegment = islandSizeAsDecimal.multiply(BigDecimal.valueOf((1 - this.moutainWidths - this.plateauWidths) / 2));
		k1 = islandSizeAsDecimal.multiply(BigDecimal.valueOf(this.plateauWidths / 2.0)).round(new MathContext(1, RoundingMode.CEILING));
		p1 = k1.add(mountainSegment);
		k2 = p1.add(mountainSegment);
		median = k2.add(abyssSegment);
		k3 = median.add(abyssSegment);
		p2 = k3.add(mountainSegment);
		k4 = p2.add(mountainSegment);
		if (k4.compareTo(islandSizeAsDecimal) >= 0) {
			throw new IllegalStateException();
		}

		double val;
		BigDecimal absolutePlateauHeight = BigDecimal.valueOf(plateauOfIsland);
		BigDecimal absoluteMountainHeight = BigDecimal.valueOf(plateauOfIsland).add(BigDecimal.valueOf(this.mountainHeight));
		BigDecimal absoluteAbyssDepth = BigDecimal.valueOf(plateauOfIsland).subtract(BigDecimal.valueOf(this.abyssDepth));

		if (positionOnIsland.compareTo(k1.toBigInteger()) <= 0 || positionOnIsland.compareTo(k4.toBigInteger()) > 0) {
			val = plateauOfIsland;
		}
		else if (positionOnIsland.compareTo(p1.toBigInteger()) <= 0) {
			val = new AffineFunction(k1, BigDecimal.valueOf(plateauOfIsland), p1, absoluteMountainHeight).applyAsDouble(positionOnIsland);
		}
		else if (positionOnIsland.compareTo(k2.toBigInteger()) <= 0) {
			val = new AffineFunction(p1, absoluteMountainHeight, k2, absolutePlateauHeight).applyAsDouble(positionOnIsland);
		}
		else if (positionOnIsland.compareTo(median.toBigInteger()) <= 0) {
			val = new AffineFunction(k2, absolutePlateauHeight, median, absoluteAbyssDepth).applyAsDouble(positionOnIsland);
		}
		else if (positionOnIsland.compareTo(k3.toBigInteger()) <= 0) {
			val = new AffineFunction(median, absoluteAbyssDepth, k3, absolutePlateauHeight).applyAsDouble(positionOnIsland);
		}
		else if (positionOnIsland.compareTo(p2.toBigInteger()) <= 0) {
			val = new AffineFunction(k3, absolutePlateauHeight, p2, absoluteMountainHeight).applyAsDouble(positionOnIsland);
		}
		else if (positionOnIsland.compareTo(k4.toBigInteger()) <= 0) {
			val = new AffineFunction(p2, absoluteMountainHeight, k4, absolutePlateauHeight).applyAsDouble(positionOnIsland);
		}
		else {
			throw new IllegalStateException("This case should never occur!");
		}
		return val;
	}

	@Override
	public double getMinimumAchievable() {
		return this.minimumAchievable;
	}
}
