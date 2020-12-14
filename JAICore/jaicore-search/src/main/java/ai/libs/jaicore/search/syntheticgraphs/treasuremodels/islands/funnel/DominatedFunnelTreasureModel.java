package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.funnel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.AIslandTreasureModel;

public class DominatedFunnelTreasureModel extends AIslandTreasureModel {

	public DominatedFunnelTreasureModel(final IIslandModel islandModel, final Random random) {
		super(islandModel);
		this.random = random;
		this.seed = random.nextLong();
	}

	private final Random random;
	private final long seed;
	private final int numberOfTreasureIslands = 1;

	private final double plateauMin = 0.5;
	private final double maxPlateauAdvantageOfSubOptimals = 0.2;
	private double bestPlateauOfTreasures;
	private final double relativeInnerWidth = 0.1;
	private final double absSlopeOfInnerPlateau = 0.001;
	private final double relativeFunnelWidth = Math.pow(10, -10);

	private final Set<BigInteger> indicesOfTreasureIslands = new HashSet<>();
	private final Map<BigInteger, Double> plateausOfIslands = new HashMap<>();

	private void distributeTreasures() {
		Random localRandom = new Random(this.seed);
		this.bestPlateauOfTreasures = Double.MAX_VALUE;

		/* determine indices of treasure island(s) */
		while (this.indicesOfTreasureIslands.size() < this.numberOfTreasureIslands) {
			BigInteger newTreasureIsland;
			do {
				newTreasureIsland = new BigInteger(this.getIslandModel().getNumberOfIslands().bitLength(), localRandom);
			}
			while (newTreasureIsland.compareTo(this.getIslandModel().getNumberOfIslands()) >= 0);
			this.indicesOfTreasureIslands.add(newTreasureIsland);
		}

		/* compute the quality of the treasure islands */
		for (BigInteger island : this.indicesOfTreasureIslands) {
			double plateauOfThisIsland = this.plateauMin + (.9 - this.plateauMin) * localRandom.nextDouble();
			this.plateausOfIslands.put(island, plateauOfThisIsland);
			this.bestPlateauOfTreasures = Math.min(this.bestPlateauOfTreasures, plateauOfThisIsland);
		}

		this.logger.info("Treasure plateaus: {}. Treasure island: {}", this.getInnerPlateauOfTreasureIsland(this.bestPlateauOfTreasures), this.indicesOfTreasureIslands);
	}

	@Override
	public Double evaluate(final ILabeledPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException {
		if (this.indicesOfTreasureIslands.isEmpty()) {
			this.getIslandModel().setRootNode(path.getRoot());
			this.distributeTreasures();
		}
		BigInteger positionOnIsland = this.getPositionOnIsland(path);
		BigInteger island = this.getIsland(path);
		if (!this.plateausOfIslands.containsKey(island)) {
			this.plateausOfIslands.put(island, this.bestPlateauOfTreasures - this.maxPlateauAdvantageOfSubOptimals * (.5 - new Random(path.hashCode() + this.seed).nextDouble()));
		}
		double plateauOfIsland = this.plateausOfIslands.get(island);

		/* if this is not a treasure island, just return the plateau value */
		if (!this.indicesOfTreasureIslands.contains(island)) {
			return plateauOfIsland;
		}

		/* compute the relative position of the solution on the island */
		BigInteger islandSize = this.getIsland(path);
		if (positionOnIsland.compareTo(islandSize) > 0) {
			throw new IllegalStateException("Position on island cannot be greater than the island itself.");
		}
		double relativePositionOnIsland = new BigDecimal(positionOnIsland).divide(new BigDecimal(islandSize)).doubleValue();

		/* if the position is not in the inner part, return the plateau value */
		double massOfBadPlateau = 1 - this.relativeInnerWidth;
		if (relativePositionOnIsland < massOfBadPlateau / 2 || (1 - relativePositionOnIsland) < massOfBadPlateau / 2) {
			return plateauOfIsland;
		}

		/* if the position is in the inner part but not within the full, return the good plateau */
		double marginOfFunnel = (1 - this.relativeFunnelWidth) / 2;
		if (relativePositionOnIsland < marginOfFunnel || (1 - relativePositionOnIsland) < marginOfFunnel) {
			double relativePositionInInnerPlateau = (relativePositionOnIsland - massOfBadPlateau / 2) / this.relativeInnerWidth;
			if (relativePositionInInnerPlateau > 1) {
				throw new IllegalStateException();
			}
			double distanceToPlateauBorder = relativePositionInInnerPlateau < .5 ? relativePositionInInnerPlateau : (1 - relativePositionInInnerPlateau);
			double plateauMax = this.getInnerPlateauOfTreasureIsland(this.bestPlateauOfTreasures);
			return plateauMax - this.absSlopeOfInnerPlateau * distanceToPlateauBorder;
		}
		return this.random.nextDouble() * .1;
	}

	private double getInnerPlateauOfTreasureIsland(final double nivel) {
		return nivel - .5 * this.maxPlateauAdvantageOfSubOptimals - 0.01;
	}

	@Override
	public double getMinimumAchievable() {
		return 0.0;
	}


	@Override
	public boolean isPathToTreasureIsland(final ILabeledPath<ITransparentTreeNode, Integer> path) {
		return this.indicesOfTreasureIslands.contains(this.getIsland(path));
	}
}
