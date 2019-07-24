package ai.libs.jaicore.search.syntheticgraphs;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.search.syntheticgraphs.BalancedGraphGeneratorGenerator.N;

public class TreasureIslandPathCostGenerator implements IPathEvaluator<N, Integer, Double> {

	private final int distanceToIslands; // level on which the islands are reached (nodes below explain what happens on the islands)
	private final Set<Integer> indicesOfIslands = new HashSet<>();

	public TreasureIslandPathCostGenerator(final int numberOfIslandsWithTreasure, final int distanceToIslands, final int numberOfIslands) {
		this(numberOfIslandsWithTreasure, distanceToIslands, numberOfIslands, 0);
	}

	public TreasureIslandPathCostGenerator(final int numberOfIslandsWithTreasure, final int distanceToIslands, final int numberOfIslands, final long seed) {
		this(numberOfIslandsWithTreasure, distanceToIslands, numberOfIslands, new Random(seed));
	}

	public TreasureIslandPathCostGenerator(final int numberOfIslandsWithTreasure, final int distanceToIslands, final int numberOfIslands, final Random r) {
		super();
		Set<Integer> possibleIndices = new HashSet<>();
		for (int i = 0; i < numberOfIslands; i++) {
			possibleIndices.add(i);
		}
		for (int i = 0; i < numberOfIslandsWithTreasure; i++) {
			int index = SetUtil.getRandomElement(possibleIndices, i);
			possibleIndices.remove(index);
			this.indicesOfIslands.add(index);
		}
		this.distanceToIslands = distanceToIslands;
	}

	public boolean isTreasurePath(final IPath<N, Integer> path) {
		return this.indicesOfIslands.contains(path.getNodes().get(TreasureIslandPathCostGenerator.this.distanceToIslands).idOfNodeOnLayer);
	}

	@Override
	public Double evaluate(final IPath<N, Integer> path) throws PathEvaluationException, InterruptedException {
		Random r = new Random(path.getNodes().hashCode());
		return this.isTreasurePath(path) ? r.nextDouble() : 1 + r.nextDouble() * 100;
	}
}
