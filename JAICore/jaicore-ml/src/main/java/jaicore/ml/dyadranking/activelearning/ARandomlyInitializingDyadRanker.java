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

	public ARandomlyInitializingDyadRanker(final PLNetDyadRanker ranker, final IDyadRankingPoolProvider poolProvider, final int seed, final int numberRandomQueriesAtStart, final int minibatchSize) {
		super(ranker, poolProvider);
		this.dyadStats = new HashMap<>();
		this.instanceFeatures = new ArrayList<>(poolProvider.getInstanceFeatures());
		this.numberRandomQueriesAtStart = numberRandomQueriesAtStart;
		this.minibatchSize = minibatchSize;
		this.iteration = 0;
		for (Vector instance : this.instanceFeatures) {
			for (Dyad dyad : poolProvider.getDyadsByInstance(instance)) {
				this.dyadStats.put(dyad, new SummaryStatistics());
			}
		}
		this.random = new Random(seed);
	}

	@Override
	public void activelyTrain(final int numberOfQueries) throws TrainingException {
		for (int i = 0; i < numberOfQueries; i++) {
			if (this.iteration < this.numberRandomQueriesAtStart) {
				Set<IDyadRankingInstance> minibatch = new HashSet<>();
				for (int batchIndex = 0; batchIndex < this.minibatchSize; batchIndex++) {
					// get random instance
					Collections.shuffle(this.instanceFeatures, this.random);
					if (this.instanceFeatures.isEmpty()) {
						break;
					}
					Vector instance = this.instanceFeatures.get(0);

					// get random pair of dyads
					List<Dyad> dyads = new ArrayList<>(this.poolProvider.getDyadsByInstance(instance));
					Collections.shuffle(dyads, this.random);

					// query them
					LinkedList<Vector> alternatives = new LinkedList<>();
					alternatives.add(dyads.get(0).getAlternative());
					alternatives.add(dyads.get(1).getAlternative());
					SparseDyadRankingInstance queryInstance = new SparseDyadRankingInstance(dyads.get(0).getInstance(), alternatives);
					IDyadRankingInstance trueRanking = this.poolProvider.query(queryInstance);
					minibatch.add(trueRanking);
				}
				// feed it to the ranker
				try {
					this.updateRanker(minibatch);
				} catch (TrainingException e) {
					this.logger.error(e.getMessage());
				}
			} else {
				this.activelyTrainWithOneInstance();
			}
			this.iteration ++;
		}
	}

	public int getNumberRandomQueriesAtStart() {
		return this.numberRandomQueriesAtStart;
	}

	public int getIteration() {
		return this.iteration;
	}

	public Map<Dyad, SummaryStatistics> getDyadStats() {
		return this.dyadStats;
	}

	public List<Vector> getInstanceFeatures() {
		return this.instanceFeatures;
	}

	public Random getRandom() {
		return this.random;
	}

	public int getMinibatchSize() {
		return this.minibatchSize;
	}

	@Override
	public abstract void activelyTrainWithOneInstance() throws TrainingException;


	public void updateRanker(final Set<IDyadRankingInstance> minibatch) throws TrainingException {

		this.ranker.update(minibatch);
		// update variances (confidence)
		for (Vector inst : this.getInstanceFeatures()) {
			for (Dyad dyad : this.poolProvider.getDyadsByInstance(inst)) {
				double skill = this.ranker.getSkillForDyad(dyad);
				this.dyadStats.get(dyad).addValue(skill);
			}
		}
	}
}
