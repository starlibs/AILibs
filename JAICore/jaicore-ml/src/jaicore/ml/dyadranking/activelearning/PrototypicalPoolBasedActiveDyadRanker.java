package jaicore.ml.dyadranking.activelearning;

import java.util.ArrayList;
import java.util.List;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;

public class PrototypicalPoolBasedActiveDyadRanker extends ActiveDyadRanker {

	public PrototypicalPoolBasedActiveDyadRanker(PLNetDyadRanker ranker, IDyadRankingPoolProvider poolProvider) {
		super(ranker, poolProvider);
	}

	public void activelyTrain(int numberOfQueries) {
		for (int i = 0; i < numberOfQueries; i++) {

			// get the instance feature vector for which the top ranking has the lowest
			// probability, d^star in the paper
			Vector dStar = null;
			double currentLowestProb = Double.MAX_VALUE;
//			System.out.println("pool size: " + poolProvider.getInstanceFeatures().size());
			for (Vector instanceFeatures : poolProvider.getInstanceFeatures()) {
				List<Dyad> dyads = new ArrayList<Dyad>(poolProvider.getDyadsByInstance(instanceFeatures));
				IDyadRankingInstance queryRanking = new DyadRankingInstance(dyads);
				double prob = ranker.getProbabilityOfTopKRanking(queryRanking, 5);
//				double prob = ranker.getProbabilityOfTopRanking(queryRanking);
//				System.out.println("Probability of ranking with " + instanceFeatures + ": " + prob);
				if (prob < currentLowestProb) {
					currentLowestProb = prob;
					dStar = instanceFeatures;
				}
			}
			System.out.println("\ndstar: " + dStar + "with probability: " + currentLowestProb);

			List<Dyad> dyads = new ArrayList<Dyad>(poolProvider.getDyadsByInstance(dStar));
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

			try {
				ranker.update(groundTruthPair);
			} catch (TrainingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			ranker.updateIteratively(groundTruthPair);
		}
	}

}
