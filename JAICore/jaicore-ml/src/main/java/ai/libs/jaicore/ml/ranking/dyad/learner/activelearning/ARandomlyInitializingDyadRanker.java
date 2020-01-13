package ai.libs.jaicore.ml.ranking.dyad.learner.activelearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.dataset.SparseDyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;

public abstract class ARandomlyInitializingDyadRanker extends ActiveDyadRanker {

	private final Logger logger = LoggerFactory.getLogger(ARandomlyInitializingDyadRanker.class);
	private final int numberRandomQueriesAtStart;
	private final Map<IDyad, SummaryStatistics> dyadStats;
	private final List<IVector> instanceFeatures;
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
		for (IVector instance : this.instanceFeatures) {
			for (IDyad dyad : poolProvider.getDyadsByInstance(instance)) {
				this.dyadStats.put(dyad, new SummaryStatistics());
			}
		}
		this.random = new Random(seed);
	}

	@Override
	public void activelyTrain(final int numberOfQueries) throws TrainingException, InterruptedException {
		for (int i = 0; i < numberOfQueries; i++) {
			if (this.iteration < this.numberRandomQueriesAtStart) {
				DyadRankingDataset minibatch = new DyadRankingDataset();
				for (int batchIndex = 0; batchIndex < this.minibatchSize; batchIndex++) {
					// get random instance
					Collections.shuffle(this.instanceFeatures, this.random);
					if (this.instanceFeatures.isEmpty()) {
						break;
					}
					IVector instance = this.instanceFeatures.get(0);

					// get random pair of dyads
					List<IDyad> dyads = new ArrayList<>(this.poolProvider.getDyadsByInstance(instance));
					Collections.shuffle(dyads, this.random);

					// query them
					LinkedList<IVector> alternatives = new LinkedList<>();
					alternatives.add(dyads.get(0).getAlternative());
					alternatives.add(dyads.get(1).getAlternative());
					SparseDyadRankingInstance queryInstance = new SparseDyadRankingInstance(dyads.get(0).getContext(), alternatives);
					IDyadRankingInstance trueRanking = this.poolProvider.query(queryInstance);
					minibatch.add(trueRanking);
				}
				// feed it to the ranker
				try {
					this.updateRanker(minibatch);
				} catch (TrainingException e) {
					this.logger.error("Updating the dyad ranking learner did not succeed.", e);
				}
			} else {
				this.activelyTrainWithOneInstance();
			}
			this.iteration++;
		}
	}

	public int getNumberRandomQueriesAtStart() {
		return this.numberRandomQueriesAtStart;
	}

	public int getIteration() {
		return this.iteration;
	}

	public Map<IDyad, SummaryStatistics> getDyadStats() {
		return this.dyadStats;
	}

	public List<IVector> getInstanceFeatures() {
		return this.instanceFeatures;
	}

	public Random getRandom() {
		return this.random;
	}

	public int getMinibatchSize() {
		return this.minibatchSize;
	}

	@Override
	public abstract void activelyTrainWithOneInstance() throws TrainingException, InterruptedException;

	public void updateRanker(final DyadRankingDataset minibatch) throws TrainingException, InterruptedException {
		this.ranker.fit(minibatch);
		// update variances (confidence)
		for (IVector inst : this.getInstanceFeatures()) {
			for (IDyad dyad : this.poolProvider.getDyadsByInstance(inst)) {
				double skill = this.ranker.getSkillForDyad(dyad);
				this.dyadStats.get(dyad).addValue(skill);
			}
		}
	}
}
