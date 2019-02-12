package jaicore.ml.dyadranking.activelearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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

public class PrototypicalPoolBasedActiveDyadRanker extends ActiveDyadRanker {

	private static final int MAX_INSTANCES_PER_BATCH = 10;
	private static final int MAX_PAIRS_PER_INSTANCE_IN_BATCH = 1;
	private static final int LENGTH_OF_TOP_RANKING = 5;
	private static final double PORTION_OF_OLD_INSTANCES_FOR_MINIBATCH = 0.3d;

	private ArrayList<IInstance> seenInstances;

	public PrototypicalPoolBasedActiveDyadRanker(PLNetDyadRanker ranker, IDyadRankingPoolProvider poolProvider) {
		super(ranker, poolProvider);
		seenInstances = new ArrayList<IInstance>(poolProvider.getPool().size());
	}

	public void activelyTrain(int numberOfQueries) {
		for (int i = 0; i < numberOfQueries; i++) {

			// get the instance feature vector for which the top ranking has the lowest
			// probability, d^star in the paper
			Set<IInstance> minibatch = new HashSet<IInstance>();
			List<Pair<Vector, Double>> dStarWithProbability = new ArrayList<Pair<Vector, Double>>(
					MAX_INSTANCES_PER_BATCH);
			for (Vector instanceFeatures : poolProvider.getInstanceFeatures()) {
				List<Dyad> dyads = new ArrayList<Dyad>(poolProvider.getDyadsByInstance(instanceFeatures));
				IDyadRankingInstance queryRanking = new DyadRankingInstance(dyads);
				double prob = ranker.getProbabilityOfTopKRanking(queryRanking, LENGTH_OF_TOP_RANKING);
				dStarWithProbability.add(new Pair<Vector, Double>(instanceFeatures, prob));
			}

			Collections.sort(dStarWithProbability, Comparator.comparing(p -> -p.getRight()));

			int numberOfOldInstances = Integer.min((int)(PORTION_OF_OLD_INSTANCES_FOR_MINIBATCH*MAX_INSTANCES_PER_BATCH), seenInstances.size());
			int numberOfNewInstances = MAX_INSTANCES_PER_BATCH - numberOfOldInstances;
			
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

//			System.out.println("Query pair: " + queryPair.getDyadAtPosition(0).getAlternative() + " "
//					+ queryPair.getDyadAtPosition(1).getAlternative());

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
			
			// Select a portion of random instances that have already been queried and add them to the minibatch
			Collections.shuffle(seenInstances);
			List<IInstance> oldInstances = seenInstances.subList(0, numberOfOldInstances);
			minibatch.addAll(oldInstances);
			
			try {
//				System.out.println("Minibatch size: " + minibatch.size());
				ranker.update(minibatch);
			} catch (TrainingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			ranker.updateIteratively(groundTruthPair);
		}
	}

}
