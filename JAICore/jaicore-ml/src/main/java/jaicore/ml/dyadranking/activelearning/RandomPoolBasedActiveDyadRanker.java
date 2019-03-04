package jaicore.ml.dyadranking.activelearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;

public class RandomPoolBasedActiveDyadRanker extends ActiveDyadRanker {

	private Random random;
	private long seed;
	private int maxBatchSize;

	public RandomPoolBasedActiveDyadRanker(PLNetDyadRanker ranker, IDyadRankingPoolProvider poolProvider, long seed) {
		super(ranker, poolProvider);
		this.seed = seed;
	}

	public RandomPoolBasedActiveDyadRanker(PLNetDyadRanker ranker, IDyadRankingPoolProvider poolProvider,
			int maxBatchSize, long seed) {
		super(ranker, poolProvider);
		this.maxBatchSize = maxBatchSize;
		this.seed = seed;
	}

	@Override
	public void activelyTrain(int numberOfQueries) {
		random = new Random(seed);
		for (int i = 0; i < numberOfQueries; i++) {
			Set<IInstance> minibatch = new HashSet<IInstance>();
			for (int batchIndex = 0; batchIndex < maxBatchSize; batchIndex++) {
				// get random instance
				List<Vector> instanceFeatures = new ArrayList<Vector>(poolProvider.getInstanceFeatures());
				Collections.shuffle(instanceFeatures, random);
				if (instanceFeatures.isEmpty())
					break;
				Vector instance = instanceFeatures.get(0);

				// get two random pair of dyads
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	public void setMaxBatchSize(int maxBatchSize) {
		this.maxBatchSize = maxBatchSize;
	}

}
