package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.noisymean;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;

/**
 * In this model, every island has a mean, and the scores are distributed closely around this mean.
 *
 * The mean itself is defined by a more concrete subclass.
 *
 * @author fmohr
 *
 * @param <N>
 */
public abstract class NoisyMeanTreasureModel implements ITreasureModel {

	private final IIslandModel islandModel;

	public NoisyMeanTreasureModel(final IIslandModel islandModel) {
		super();
		this.islandModel = islandModel;
	}

	public abstract double getMeanOfIsland(long island);

	@Override
	public Double evaluate(final IPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException {
		this.islandModel.setRootNode(path.getRoot());
		long island = this.islandModel.getIsland(path);
		double mean = this.getMeanOfIsland(island);
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

	public IIslandModel getIslandModel() {
		return this.islandModel;
	}
}