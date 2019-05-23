package jaicore.ml.dyadranking.activelearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;

public abstract class ARandomlyInitializingDyadRanker extends ActiveDyadRanker {

	private final Logger logger = LoggerFactory.getLogger(ARandomlyInitializingDyadRanker.class);
	private final int numberRandomQueriesAtStart;
	private final Map<Dyad, SummaryStatistics> dyadStats;
	private final List<Vector> instanceFeatures;
	private final Random random;
	private final int minibatchSize;
	
	private int iteration;

	public ARandomlyInitializingDyadRanker(PLNetDyadRanker ranker, IDyadRankingPoolProvider poolProvider, int seed, int numberRandomQueriesAtStart, int minibatchSize) {
		super(ranker, poolProvider);
		this.dyadStats = new HashMap<>();
		this.instanceFeatures = new ArrayList<>(poolProvider.getInstanceFeatures());
		this.numberRandomQueriesAtStart = numberRandomQueriesAtStart;
		this.minibatchSize = minibatchSize;
		this.iteration = 0;
		for (Vector instance : instanceFeatures) {
			for (Dyad dyad : poolProvider.getDyadsByInstance(instance)) {
				this.dyadStats.put(dyad, new SummaryStatistics());
			}
		}
		this.random = new Random(seed);
	}

	@Override
	public void activelyTrain(int numberOfQueries) throws TrainingException {
		for (int i = 0; i < numberOfQueries; i++) {
			if (iteration < numberRandomQueriesAtStart) {
				Set<IDyadRankingInstance> minibatch = new HashSet<>();
				for (int batchIndex = 0; batchIndex < this.minibatchSize; batchIndex++) {
					// get random instance
					Collections.shuffle(instanceFeatures, random);
					if (instanceFeatures.isEmpty())
						break;
					Vector instance = instanceFeatures.get(0);

					// get random pair of dyads
					List<Dyad> dyads = new ArrayList<>(poolProvider.getDyadsByInstance(instance));
					Collections.shuffle(dyads, random);

					// query them
					LinkedList<Vector> alternatives = new LinkedList<>();
					alternatives.add(dyads.get(0).getAlternative());
					alternatives.add(dyads.get(1).getAlternative());
					SparseDyadRankingInstance queryInstance = new SparseDyadRankingInstance(dyads.get(0).getInstance(), alternatives);
					IDyadRankingInstance trueRanking = poolProvider.query(queryInstance);
					minibatch.add(trueRanking);
				}
				// feed it to the ranker
				try {
					updateRanker(minibatch);
				} catch (TrainingException e) {
					logger.error(e.getMessage());
				}
			} else {
				activelyTrainWithOneInstance();
			}
			iteration ++;
		}
	}

	public int getNumberRandomQueriesAtStart() {
		return numberRandomQueriesAtStart;
	}

	public int getIteration() {
		return iteration;
	}

	public Map<Dyad, SummaryStatistics> getDyadStats() {
		return dyadStats;
	}

	public List<Vector> getInstanceFeatures() {
		return instanceFeatures;
	}

	public Random getRandom() {
		return random;
	}

	public int getMinibatchSize() {
		return minibatchSize;
	}

	public abstract void activelyTrainWithOneInstance() throws TrainingException;

	
	public void updateRanker(Set<IDyadRankingInstance> minibatch) throws TrainingException {
		
		ranker.update(minibatch);
		// update variances (confidence)
		for (Vector inst : getInstanceFeatures()) {
			for (Dyad dyad : poolProvider.getDyadsByInstance(inst)) {
				double skill = ranker.getSkillForDyad(dyad);
				dyadStats.get(dyad).addValue(skill);
			}
		}
	}
}
