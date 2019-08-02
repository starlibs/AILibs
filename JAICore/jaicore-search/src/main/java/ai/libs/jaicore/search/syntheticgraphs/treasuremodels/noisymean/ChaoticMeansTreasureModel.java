package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.noisymean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;

/**
 * In this model, the means for the islands do not follow any pattern but are just drawn randomly.
 *
 * @author fmohr
 *
 * @param <N>
 */
public class ChaoticMeansTreasureModel extends NoisyMeanTreasureModel {
	private final int numberOfIslandsWithTreasure;
	private final Map<Long, Double> means = new HashMap<>();
	private final Random random;
	private final Set<Long> indicesOfIslands = new HashSet<>();

	public ChaoticMeansTreasureModel(final int numberOfIslandsWithTreasure, final IIslandModel islandModel, final long seed) {
		this(numberOfIslandsWithTreasure, islandModel, new Random(seed));
	}

	public ChaoticMeansTreasureModel(final int numberOfIslandsWithTreasure, final IIslandModel islandModel, final Random r) {
		super(islandModel);
		this.numberOfIslandsWithTreasure = numberOfIslandsWithTreasure;
		this.random = r;
	}

	private void distributeTreasures() {
		while (this.indicesOfIslands.size() < this.numberOfIslandsWithTreasure) {
			this.indicesOfIslands.add((long) this.random.nextInt(Math.abs((int)this.getIslandModel().getNumberOfIslands())));
		}
	}

	@Override
	public double getMeanOfIsland(final long island) {
		if (this.indicesOfIslands.isEmpty()) {
			this.distributeTreasures();
		}
		final Random r1 = new Random(this.random.nextInt() + island); // this randomness includes the random source of the generator
		return this.means.computeIfAbsent(island, p -> this.indicesOfIslands.contains(p) ? 1 + r1.nextDouble() * 5 : 20 + r1.nextDouble() * 85);
	}

	public boolean isTreasureIsland(final long island) {
		return this.indicesOfIslands.contains(island);
	}

	public boolean isPathToTreasureIsland(final IPath<ITransparentTreeNode, Integer> path) {
		return this.isTreasureIsland(this.getIslandModel().getIsland(path));
	}
}
