package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.search.algorithms.standard.random.RandomSearch;
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
	private final Map<BigInteger, Double> means = new HashMap<>();
	private final Random random;
	private final Set<BigInteger> indicesOfIslands = new HashSet<>();
	private boolean treasuresDistributed = false;
	private IPathSearchInput<ITransparentTreeNode, Integer> graphSearchInput;

	public ChaoticMeansTreasureModel(final int numberOfIslandsWithTreasure, final IIslandModel islandModel, final long seed) {
		this(numberOfIslandsWithTreasure, islandModel, new Random(seed));
	}

	public ChaoticMeansTreasureModel(final int numberOfIslandsWithTreasure, final IIslandModel islandModel, final Random r) {
		super(islandModel);
		this.numberOfIslandsWithTreasure = numberOfIslandsWithTreasure;
		this.random = r;
	}

	private void distributeTreasures() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		if (this.graphSearchInput == null) {
			throw new IllegalStateException("Cannot distribute treasures before graph generator has been set.");
		}
		this.logger.info("Start treasure distribution. Will choose {} treasure islands.", this.numberOfIslandsWithTreasure);
		RandomSearch<ITransparentTreeNode, Integer> rs = new RandomSearch<>(this.graphSearchInput);
		while (this.indicesOfIslands.size() < this.numberOfIslandsWithTreasure) {
			ILabeledPath<ITransparentTreeNode, Integer> treasurePath = rs.nextSolutionCandidate();
			this.indicesOfIslands.add(this.getIslandModel().getIsland(treasurePath));
		}
		if (this.indicesOfIslands.size() != this.numberOfIslandsWithTreasure) {
			throw new IllegalStateException("Treasure distribution failed! Distributed " + this.indicesOfIslands.size() + " instead of " + this.numberOfIslandsWithTreasure + " treasurs.");
		}
		this.logger.info("Defined {} treasure islands: {}", this.numberOfIslandsWithTreasure, this.indicesOfIslands);
		this.treasuresDistributed = true;
	}

	@Override
	public double getMeanOfIsland(final BigInteger island) {
		if (this.indicesOfIslands.isEmpty()) {
			try {
				this.distributeTreasures();
			} catch (AlgorithmTimeoutedException | AlgorithmExecutionCanceledException | AlgorithmException e) {
				return Double.NaN;
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return Double.NaN;
			}
		}
		final Random r1 = new Random(this.random.nextInt() + (long)island.intValue()); // this randomness includes the random source of the generator
		return this.means.computeIfAbsent(island, p -> this.isTreasureIsland(p) ? 1 + r1.nextDouble() * 5 : 20 + r1.nextDouble() * 85);
	}

	public boolean isTreasureIsland(final BigInteger island) {
		return this.indicesOfIslands.contains(island);
	}

	public boolean isPathToTreasureIsland(final ILabeledPath<ITransparentTreeNode, Integer> path) {
		return this.isTreasureIsland(this.getIslandModel().getIsland(path));
	}

	public Collection<BigInteger> getTreasureIslands() {
		return Collections.unmodifiableCollection(this.indicesOfIslands);
	}

	@Override
	public double getMinimumAchievable() {
		throw new UnsupportedOperationException();
	}

	public boolean isTreasuresDistributed() {
		return this.treasuresDistributed;
	}

	public IPathSearchInput<ITransparentTreeNode, Integer> getGraphSearchInput() {
		return this.graphSearchInput;
	}

	public void setGraphSearchInput(final IPathSearchInput<ITransparentTreeNode, Integer> graphSearchInput) {
		this.graphSearchInput = graphSearchInput;
	}
}
