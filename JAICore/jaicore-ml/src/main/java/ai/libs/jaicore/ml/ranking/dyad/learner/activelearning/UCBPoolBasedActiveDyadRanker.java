package ai.libs.jaicore.ml.ranking.dyad.learner.activelearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;
import org.nd4j.linalg.primitives.Pair;

import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.dataset.SparseDyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;

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
public class UCBPoolBasedActiveDyadRanker extends ARandomlyInitializingDyadRanker {

	public UCBPoolBasedActiveDyadRanker(final PLNetDyadRanker ranker, final IDyadRankingPoolProvider poolProvider, final int seed, final int numberRandomQueriesAtStart, final int minibatchSize) {
		super(ranker, poolProvider, seed, numberRandomQueriesAtStart, minibatchSize);
	}

	@Override
	public void activelyTrainWithOneInstance() throws TrainingException, InterruptedException {

		DyadRankingDataset minibatch = new DyadRankingDataset();
		for (int minibatchIndex = 0; minibatchIndex < this.getMinibatchSize(); minibatchIndex++) {

			// randomly choose dataset to sample from
			int index = this.getRandom().nextInt(this.getInstanceFeatures().size());
			IVector problemInstance = this.getInstanceFeatures().get(index);

			// update empirical standard deviation and compute upper confidence bound for
			// each dyad
			// from this dataset
			List<IDyad> dyads = new ArrayList<>(this.poolProvider.getDyadsByInstance(problemInstance));
			List<Pair<IDyad, Double>> dyadsWithUCB = new ArrayList<>(dyads.size());
			for (IDyad dyad : dyads) {
				double skill = this.ranker.getSkillForDyad(dyad);
				double std = this.getDyadStats().get(dyad).getStandardDeviation();
				double ucb = skill + std;
				dyadsWithUCB.add(new Pair<>(dyad, ucb));
			}

			// query the two dyads with highest ucb
			Collections.sort(dyadsWithUCB, Comparator.comparing(p -> -p.getRight()));
			IDyad d1 = dyadsWithUCB.get(0).getFirst();
			IDyad d2 = dyadsWithUCB.get(1).getFirst();
			List<IVector> alts = new ArrayList<>(2);
			alts.add(d1.getAlternative());
			alts.add(d2.getAlternative());

			SparseDyadRankingInstance sparseQueryPair = new SparseDyadRankingInstance(d1.getContext(), alts);

			IDyadRankingInstance groundTruthPair = this.poolProvider.query(sparseQueryPair);

			// add it to the minibatch
			minibatch.add(groundTruthPair);
		}

		// update the ranker
		this.updateRanker(minibatch);
	}
}
