package jaicore.ml.dyadranking.activelearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;

public class UCBPoolBasedActiveDyadRanker extends ActiveDyadRanker {

	private HashMap<Dyad, SummaryStatistics> dyadStats;
	private List<Vector> instanceFeatures;
	private Random random;
	
	public UCBPoolBasedActiveDyadRanker(PLNetDyadRanker ranker, IDyadRankingPoolProvider poolProvider, int seed) {
		super(ranker, poolProvider);
		this.dyadStats = new HashMap<Dyad, SummaryStatistics>();
		this.instanceFeatures = new ArrayList<Vector>(poolProvider.getInstanceFeatures());
		for(Vector instance : instanceFeatures) {
			for(Dyad dyad : poolProvider.getDyadsByInstance(instance)) {
				this.dyadStats.put(dyad, new SummaryStatistics());
			}
		}
		this.random = new Random(seed);
		
	}

	@Override
	public void activelyTrain(int numberOfQueries) {
		
		for(int i = 0; i < numberOfQueries; i++) {
			
			// randomly choose dataset to sample from
			Collections.shuffle(instanceFeatures, random);
			
			
			// compute upper confidence bound for each dyad from this dataset
			
			// query the two dyads with highest ucb
			
			//			
			
		}
		
	}

}
