package jaicore.ml.dyadranking.activelearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.nd4j.linalg.primitives.Pair;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;

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
public class PrototypicalPoolBasedActiveDyadRanker extends ActiveDyadRanker {

	private ArrayList<IInstance> seenInstances;
	private int maxBatchSize;
	private double ratioOfOldInstancesForMinibatch;
	private int lengthOfTopRankingToConsider;
	private int numberRandomQueriesAtStart;
	private int iteration;
	private Random random;

	public PrototypicalPoolBasedActiveDyadRanker(PLNetDyadRanker ranker, IDyadRankingPoolProvider poolProvider,
			int maxBatchSize, int lengthOfTopRankingToConsider, double ratioOfOldInstancesForMinibatch,
			int numberRandomQueriesAtStart, int seed) {
		super(ranker, poolProvider);
		seenInstances = new ArrayList<IInstance>(poolProvider.getPool().size());
		this.maxBatchSize = maxBatchSize;
		this.ratioOfOldInstancesForMinibatch = ratioOfOldInstancesForMinibatch;
		this.lengthOfTopRankingToConsider = lengthOfTopRankingToConsider;
		this.numberRandomQueriesAtStart = numberRandomQueriesAtStart;
		this.iteration = 0;
		this.random = new Random(seed);
	}

	public void activelyTrain(int numberOfQueries) {

		if (iteration < numberRandomQueriesAtStart) {

			for (int i = 0; i < numberOfQueries; i++) {
				Set<IInstance> minibatch = new HashSet<IInstance>();
				for (int batchIndex = 0; batchIndex < maxBatchSize; batchIndex++) {
					// get random instance
					List<Vector> instanceFeatures = new ArrayList<Vector>(poolProvider.getInstanceFeatures());
					Collections.shuffle(instanceFeatures, random);
					if (instanceFeatures.isEmpty())
						break;
					Vector instance = instanceFeatures.get(0);

					// get random pair of dyads
					List<Dyad> dyads = new ArrayList<Dyad>(poolProvider.getDyadsByInstance(instance));
					Collections.shuffle(dyads, random);

					// query them
					LinkedList<Vector> alternatives = new LinkedList<Vector>();
					alternatives.add(dyads.get(0).getAlternative());
					alternatives.add(dyads.get(1).getAlternative());
					SparseDyadRankingInstance queryInstance = new SparseDyadRankingInstance(dyads.get(0).getInstance(),
							alternatives);
					IDyadRankingInstance trueRanking = (IDyadRankingInstance) poolProvider.query(queryInstance);
					minibatch.add(trueRanking);
				}
				// feed it to the ranker
				try {
					ranker.update(minibatch);
				} catch (TrainingException e) {
					e.printStackTrace();
				}
				iteration++;
			}
		}

		else {

			for (int i = 0; i < numberOfQueries; i++) {

				// get the instance feature vector for which the top ranking has the lowest
				// probability, d^star in the paper
				Set<IInstance> minibatch = new HashSet<IInstance>();
				List<Pair<Vector, Double>> dStarWithProbability = new ArrayList<Pair<Vector, Double>>(maxBatchSize);
				for (Vector instanceFeatures : poolProvider.getInstanceFeatures()) {
					dStarWithProbability.add(new Pair<Vector, Double>(instanceFeatures, 54d));
				}

				Collections.shuffle(dStarWithProbability);

				int numberOfOldInstances = Integer.min((int) (ratioOfOldInstancesForMinibatch * maxBatchSize),
						seenInstances.size());
				int numberOfNewInstances = maxBatchSize - numberOfOldInstances;

				for (int batchIndex = 0; batchIndex < numberOfNewInstances; batchIndex++) {
					Vector curDStar = dStarWithProbability.get(batchIndex).getFirst();
					List<Dyad> dyads = new ArrayList<Dyad>(poolProvider.getDyadsByInstance(curDStar));
					if (dyads.size() < 2)
						break;
					Vector instance = dyads.get(0).getInstance();
					List<Vector> alternatives = new ArrayList<Vector>(dyads.size());
					for (Dyad dyad : dyads)
						alternatives.add(dyad.getAlternative());

					SparseDyadRankingInstance queryRanking = new SparseDyadRankingInstance(instance, alternatives);

					// get the alternatives pair for which the PLNet is most uncertain
					DyadRankingInstance queryPair = ranker.getPairWithLeastCertainty(queryRanking);

					// convert to SparseDyadRankingInstance
					List<Vector> alternativePair = new ArrayList<Vector>(queryPair.length());
					for (Dyad dyad : queryPair)
						alternativePair.add(dyad.getAlternative());
					SparseDyadRankingInstance sparseQueryPair = new SparseDyadRankingInstance(
							queryPair.getDyadAtPosition(0).getInstance(), alternativePair);

					// query the pool provider to get the ground truth ranking for the pair
					IDyadRankingInstance groundTruthPair = (IDyadRankingInstance) poolProvider.query(sparseQueryPair);
					seenInstances.add(groundTruthPair);
					minibatch.add(groundTruthPair);
				}

				// Select a portion of random instances that have already been queried and add
				// them to the minibatch
				Collections.shuffle(seenInstances);
				List<IInstance> oldInstances = seenInstances.subList(0, numberOfOldInstances);
				minibatch.addAll(oldInstances);

				try {
					ranker.update(minibatch);
				} catch (TrainingException e) {
					e.printStackTrace();
				}
				iteration++;
			}
		}
	}

	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	public void setMaxBatchSize(int maxBatchSize) {
		this.maxBatchSize = maxBatchSize;
	}

	public double getRatioOfOldInstancesForMinibatch() {
		return ratioOfOldInstancesForMinibatch;
	}

	public void setRatioOfOldInstancesForMinibatch(double ratioOfOldInstancesForMinibatch) {
		this.ratioOfOldInstancesForMinibatch = ratioOfOldInstancesForMinibatch;
	}

	public int getLengthOfTopRankingToConsider() {
		return lengthOfTopRankingToConsider;
	}

	public void setLengthOfTopRankingToConsider(int lengthOfTopRankingToConsider) {
		this.lengthOfTopRankingToConsider = lengthOfTopRankingToConsider;
	}

}
