package ai.libs.jaicore.search.syntheticgraphs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.search.syntheticgraphs.BalancedGraphGeneratorGenerator.N;

public class ChaoticTreasureIslandPathCostGenerator extends TreasureIslandPathCostGenerator {
	private final Map<Long, Double> means = new HashMap<>();
	private final Random random;
	private final Set<Long> indicesOfIslands = new HashSet<>();

	public ChaoticTreasureIslandPathCostGenerator(final int numberOfIslandsWithTreasure, final int distanceToIslands, final int numberOfIslands, final long seed) {
		this(numberOfIslandsWithTreasure, distanceToIslands, numberOfIslands, new Random(seed));
	}

	public ChaoticTreasureIslandPathCostGenerator(final int numberOfIslandsWithTreasure, final int distanceToIslands, final int numberOfIslands, final Random r) {
		super(numberOfIslandsWithTreasure, distanceToIslands, numberOfIslands);
		this.random = r;

		while (this.indicesOfIslands.size() < numberOfIslandsWithTreasure) {
			this.indicesOfIslands.add((long) r.nextInt(Math.abs(numberOfIslands)));
		}
	}

	@Override
	public double getMeanOfIsland(final long island) {
		final Random r1 = new Random(this.random.nextInt() + island); // this randomness includes the random source of the generator
		return this.means.computeIfAbsent(island, p -> this.indicesOfIslands.contains(p) ? 1 + r1.nextDouble() * 5 : 20 + r1.nextDouble() * 85);
	}

	public boolean isTreasureIsland(final long island) {
		return this.indicesOfIslands.contains(island);
	}

	public boolean isPathToTreasureIsland(final IPath<N, Integer> path) {
		return this.isTreasureIsland(this.getIslandForPath(path));
	}
}
