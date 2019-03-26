package de.upb.crc901.mlplan.metamining.activelearning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

public class ActiveDyadRankingAutoMLExample {

	private PLNetDyadRanker plNetDyadRanker;
	private HashMap<Vector, Set<Dyad>> querySpace;

	public ActiveDyadRankingAutoMLExample(String datasetPath) {
		plNetDyadRanker = new PLNetDyadRanker();
		querySpace = new HashMap<Vector, Set<Dyad>>();
	}
	
	public void queryStep() {
		
		// FIRST extract the dataset dStar for which the top ranking has the lowest probability		
		// this is the metafeature vector of the dataset for which the top ranking has the lowest probability	
		Vector dStar = null;
		double currentlyLowestProbability = Double.MAX_VALUE;
		for(Vector datasetFeatures : querySpace.keySet()) {
			// attention: this list is not ordered!			
			List<Dyad> dyads = new ArrayList<Dyad>(querySpace.get(datasetFeatures));
			// attention: this dyad ranking instance is not ordered!
			IDyadRankingInstance drInstance = new DyadRankingInstance(dyads);
			double currentProbability = plNetDyadRanker.getProbabilityOfTopRanking(drInstance);
			if(currentProbability < currentlyLowestProbability) {
				dStar = datasetFeatures;
				currentlyLowestProbability = currentProbability;
			}
		}
		
		// SECOND find the two algorithms for which the current model has the smallest certainty
		// attention: this list is not ordered!			
		List<Dyad> dyads = new ArrayList<Dyad>(querySpace.get(dStar));
		// attention: this dyad ranking instance is not ordered!
		IDyadRankingInstance drInstance = new DyadRankingInstance(dyads);
		IDyadRankingInstance queryInstance = plNetDyadRanker.getPairWithLeastCertainty(drInstance);
		
		// THIRD get true ordering for the chosen query instance
		
		// FOURTH feed the true ordering to the ranker
		try {
			plNetDyadRanker.update(queryInstance);
		} catch (TrainingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void addData(DyadRankingDataset dataset) {
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			if(!querySpace.containsKey(drInstance.getDyadAtPosition(0).getInstance())) {
				querySpace.put(drInstance.getDyadAtPosition(0).getInstance(), new HashSet<Dyad>());
			}
			for(Dyad dyad : drInstance)
				querySpace.get(drInstance.getDyadAtPosition(0).getInstance()).add(dyad);
		}
	}
}
