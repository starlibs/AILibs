package ai.libs.jaicore.search.syntheticgraphs;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.search.syntheticgraphs.BalancedGraphGeneratorGenerator.N;

public abstract class TreasureIslandPathCostGenerator implements IPathEvaluator<N, Integer, Double> {

	private final int distanceToIslands; // level on which the islands are reached (nodes below explain what happens on the islands)

	public TreasureIslandPathCostGenerator(final int numberOfIslandsWithTreasure, final int distanceToIslands, final int numberOfIslands) {
		super();
		if (numberOfIslands < 0) {
			throw new IllegalArgumentException("Must have a positive number of islands!");
		}
		this.distanceToIslands = distanceToIslands;
	}

	public long getIslandForPath(final IPath<N, Integer> path) {
		long island = path.getNodes().get(TreasureIslandPathCostGenerator.this.distanceToIslands).idOfNodeOnLayer;
		return island;
	}

	public abstract double getMeanOfIsland(long island);

	@Override
	public Double evaluate(final IPath<N, Integer> path) throws PathEvaluationException, InterruptedException {
		double mean = this.getMeanOfIsland(this.getIslandForPath(path));
		double maxDeviationFactor = mean < 10 ? mean : Math.sqrt(mean);
		final Random r2 = new Random(path.hashCode());
		boolean add = r2.nextBoolean();
		double deviation = r2.nextDouble() * maxDeviationFactor * (add ? 1 : -1);
		double score = Math.max(0, mean + deviation);

		/* avoid that sub-optimal islands come into the region below 1 and vice versa */
		if (mean < 10) {
			score = Math.min(score, 9);
		}
		else {
			score = Math.max(11, score);
		}
		return score;
	}
}
