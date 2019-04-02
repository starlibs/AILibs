package jaicore.ml.dyadranking.activelearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;

/**
 * A prototypical active dyad ranker based on the UCB decision rule. During the
 * learning procedure, it keeps track over the standard deviation of the skill
 * values predicted for a dyad. First a constant number of random queries is
 * sampled at the beginning. Then the sampling strategy randomly selects problem
 * instances and picks the two dyads with largest skill + standard deviation for
 * pairwise comparison. On each query step, this is repeated a constant number
 * of times to create a minibatch.
 * 
 * @author Jonas Hanselle
 *
 */
public class UCBPoolBasedActiveDyadRanker extends ActiveDyadRanker {
	
	private static final Logger log = LoggerFactory.getLogger(UCBPoolBasedActiveDyadRanker.class);

	private HashMap<Dyad, SummaryStatistics> dyadStats;
	private List<Vector> instanceFeatures;
	private Random random;
	private int numberRandomQueriesAtStart;
	private int iteration;
	private int minibatchSize;

	public UCBPoolBasedActiveDyadRanker(PLNetDyadRanker ranker, IDyadRankingPoolProvider poolProvider, int seed,
			int numberRandomQueriesAtStart, int minibatchSize) {
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
	public void activelyTrain(int numberOfQueries) {

		for (int i = 0; i < numberOfQueries; i++) {

			// For the first query steps, sample randomly
			if (iteration < numberRandomQueriesAtStart) {

				Set<IInstance> minibatch = new HashSet<>();
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
					SparseDyadRankingInstance queryInstance = new SparseDyadRankingInstance(dyads.get(0).getInstance(),
							alternatives);
					IDyadRankingInstance trueRanking = (IDyadRankingInstance) poolProvider.query(queryInstance);
					minibatch.add(trueRanking);
				}
				// feed it to the ranker
				try {
					ranker.update(minibatch);
					// update stds (confidence)
					for (Vector inst : instanceFeatures) {
						for (Dyad dyad : poolProvider.getDyadsByInstance(inst)) {
							double skill = ranker.getSkillForDyad(dyad);
							dyadStats.get(dyad).addValue(skill);
						}
					}
				} catch (TrainingException e) {
					log.error(e.getMessage());
				}
			}

			else {

				Set<IInstance> minibatch = new HashSet<>();
				for (int minibatchIndex = 0; minibatchIndex < minibatchSize; minibatchIndex++) {

					// randomly choose dataset to sample from
					int index = random.nextInt(instanceFeatures.size());
					Vector problemInstance = instanceFeatures.get(index);

					// update empirical standard deviation and compute upper confidence bound for
					// each dyad
					// from this dataset
					List<Dyad> dyads = new ArrayList<>(poolProvider.getDyadsByInstance(problemInstance));
					List<Pair<Dyad, Double>> dyadsWithUCB = new ArrayList<>(dyads.size());
					for (Dyad dyad : dyads) {
						double skill = ranker.getSkillForDyad(dyad);
						double std = dyadStats.get(dyad).getStandardDeviation();
						double ucb = skill + std;
						dyadsWithUCB.add(new Pair<Dyad, Double>(dyad, ucb));
					}

					// query the two dyads with highest ucb
					Collections.sort(dyadsWithUCB, Comparator.comparing(p -> -p.getRight()));
					Dyad d1 = dyadsWithUCB.get(0).getFirst();
					Dyad d2 = dyadsWithUCB.get(1).getFirst();
					List<Vector> alts = new ArrayList<>(2);
					alts.add(d1.getAlternative());
					alts.add(d2.getAlternative());

					SparseDyadRankingInstance sparseQueryPair = new SparseDyadRankingInstance(d1.getInstance(), alts);

					IInstance groundTruthPair = poolProvider.query(sparseQueryPair);

					// add it to the minibatch
					minibatch.add(groundTruthPair);
				}

				// update the ranker
				try {
					ranker.update(minibatch);
					// update stds (confidence)
					for (Vector inst : instanceFeatures) {
						for (Dyad dyad : poolProvider.getDyadsByInstance(inst)) {
							double skill = ranker.getSkillForDyad(dyad);
							dyadStats.get(dyad).addValue(skill);
						}
					}
				} catch (TrainingException e) {
					log.error(e.getMessage());
				}
			}
			iteration++;
		}
	}
}
