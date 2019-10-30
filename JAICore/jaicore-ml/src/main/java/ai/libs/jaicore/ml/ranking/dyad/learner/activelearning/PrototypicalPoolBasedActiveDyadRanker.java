package ai.libs.jaicore.ml.ranking.dyad.learner.activelearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;
import org.nd4j.linalg.primitives.Pair;

import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.dataset.SparseDyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;

/**
 * A prototypical active dyad ranker based on the idea of uncertainty sampling.
 * First a constant number of random queries is sampled at the beginning. Then
 * the sampling strategy randomly selects a problem instance in each query step.
 * Afterwards it selects those two alternatives for pairwise comparison, for
 * which the difference of the skill values is minimal, as these are the pairs
 * the Plackett Luce model is least certain about. This procedure is repeated a
 * constant number of times to create a minibatch for updating the model.
 *
 * @author Jonas Hanselle
 *
 */
public class PrototypicalPoolBasedActiveDyadRanker extends ARandomlyInitializingDyadRanker {

	private ArrayList<IDyadRankingInstance> seenInstances;
	private double ratioOfOldInstancesForMinibatch;
	private int lengthOfTopRankingToConsider;

	public PrototypicalPoolBasedActiveDyadRanker(final PLNetDyadRanker ranker, final IDyadRankingPoolProvider poolProvider, final int maxBatchSize, final int lengthOfTopRankingToConsider, final double ratioOfOldInstancesForMinibatch,
			final int numberRandomQueriesAtStart, final int seed) {
		super(ranker, poolProvider, seed, numberRandomQueriesAtStart, maxBatchSize);
		this.seenInstances = new ArrayList<>(poolProvider.getPool().size());
		this.ratioOfOldInstancesForMinibatch = ratioOfOldInstancesForMinibatch;
		this.lengthOfTopRankingToConsider = lengthOfTopRankingToConsider;
	}

	@Override
	public void activelyTrainWithOneInstance() throws TrainingException, InterruptedException {

		// get the instance feature vector for which the top ranking has the lowest
		// probability, d^star in the paper
		DyadRankingDataset minibatch = new DyadRankingDataset();
		List<Pair<IVector, Double>> dStarWithProbability = new ArrayList<>(this.getMinibatchSize());
		for (IVector instanceFeatures : this.poolProvider.getInstanceFeatures()) {
			dStarWithProbability.add(new Pair<>(instanceFeatures, 54d));
		}

		Collections.shuffle(dStarWithProbability);

		int numberOfOldInstances = Integer.min((int) (this.ratioOfOldInstancesForMinibatch * this.getMinibatchSize()), this.seenInstances.size());
		int numberOfNewInstances = this.getMinibatchSize() - numberOfOldInstances;

		for (int batchIndex = 0; batchIndex < numberOfNewInstances; batchIndex++) {
			IVector curDStar = dStarWithProbability.get(batchIndex).getFirst();
			List<IDyad> dyads = new ArrayList<>(this.poolProvider.getDyadsByInstance(curDStar));
			if (dyads.size() < 2) {
				break;
			}
			IVector instance = dyads.get(0).getContext();
			List<IVector> alternatives = new ArrayList<>(dyads.size());
			for (IDyad dyad : dyads) {
				alternatives.add(dyad.getAlternative());
			}

			SparseDyadRankingInstance queryRanking = new SparseDyadRankingInstance(instance, alternatives);

			// get the alternatives pair for which the PLNet is most uncertain
			IDyadRankingInstance queryPair = this.ranker.getPairWithLeastCertainty(queryRanking);

			// convert to SparseDyadRankingInstance
			List<IVector> alternativePair = new ArrayList<>(queryPair.getNumberOfRankedElements());
			for (IDyad dyad : queryPair) {
				alternativePair.add(dyad.getAlternative());
			}
			SparseDyadRankingInstance sparseQueryPair = new SparseDyadRankingInstance(queryPair.getLabel().get(0).getContext(), alternativePair);

			// query the pool provider to get the ground truth ranking for the pair
			IDyadRankingInstance groundTruthPair = this.poolProvider.query(sparseQueryPair);
			this.seenInstances.add(groundTruthPair);
			minibatch.add(groundTruthPair);
		}

		// Select a portion of random instances that have already been queried and add
		// them to the minibatch
		Collections.shuffle(this.seenInstances);
		List<IDyadRankingInstance> oldInstances = this.seenInstances.subList(0, numberOfOldInstances);
		minibatch.addAll(oldInstances);
		this.updateRanker(minibatch);
	}

	public double getRatioOfOldInstancesForMinibatch() {
		return this.ratioOfOldInstancesForMinibatch;
	}

	public void setRatioOfOldInstancesForMinibatch(final double ratioOfOldInstancesForMinibatch) {
		this.ratioOfOldInstancesForMinibatch = ratioOfOldInstancesForMinibatch;
	}

	public int getLengthOfTopRankingToConsider() {
		return this.lengthOfTopRankingToConsider;
	}

	public void setLengthOfTopRankingToConsider(final int lengthOfTopRankingToConsider) {
		this.lengthOfTopRankingToConsider = lengthOfTopRankingToConsider;
	}

}
