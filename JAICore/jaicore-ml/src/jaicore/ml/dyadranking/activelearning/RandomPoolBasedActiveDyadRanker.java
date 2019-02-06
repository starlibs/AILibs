package jaicore.ml.dyadranking.activelearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;

public class RandomPoolBasedActiveDyadRanker extends ActiveDyadRanker {

	private Random random;
	private long seed;
	
	public RandomPoolBasedActiveDyadRanker(PLNetDyadRanker ranker, IDyadRankingPoolProvider poolProvider, long seed) {
		super(ranker, poolProvider);
		this.seed = seed;
	}

	@Override
	public void activelyTrain(int numberOfQueries) {
		seed++;
		random = new Random(seed);
		for (int i = 0; i < numberOfQueries; i++) {

			// get random instance
			List<Vector> instanceFeatures = new ArrayList<Vector>(poolProvider.getInstanceFeatures());
			Collections.shuffle(instanceFeatures, random);
			Vector instance = instanceFeatures.get(0);
			
			// get two random pair of dyads
			List<Dyad> dyads = new ArrayList<Dyad>(poolProvider.getDyadsByInstance(instance));
			Collections.shuffle(dyads, random);
			
			// query them
			DyadRankingInstance queryInstance = new DyadRankingInstance(dyads.subList(0, 2));
			DyadRankingInstance trueRanking = (DyadRankingInstance) poolProvider.query(queryInstance);
			
			// feed it to the ranker
			try {
				ranker.update(trueRanking);
			} catch (TrainingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
