package jaicore.ml.dyadranking.loss;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.IDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Class that contains utility methods for handling dyad ranking losses.
 * 
 * @author Jonas Hanselle, Helena Graf
 *
 */
public class DyadRankingLossUtil {
	
	private DyadRankingLossUtil() {
		// intentionally left blank
	}

	/**
	 * Computes the average loss over several dyad orderings.
	 * 
	 * @param lossFunction
	 *            The loss function to be used for the individual
	 *            {@link IDyadRankingInstance}s
	 * @param trueOrderings
	 *            The true orderings represented by {@link IDyadRankingInstance}s
	 * @param predictedOrderings
	 *            The predicted orderings represented by
	 *            {@link IDyadRankingInstance}s
	 * @return Average loss over all {@link IDyadRankingInstance}s
	 */
	public static double computeAverageLoss(DyadRankingLossFunction lossFunction, DyadRankingDataset trueOrderings,
			DyadRankingDataset predictedOrderings) {
		if (trueOrderings.size() != predictedOrderings.size()) {
			throw new IllegalArgumentException(
					"The list of predictions and the list of ground truth dyad rankings need to have the same length!");
		}
		double avgLoss = 0.0d;
		for (int i = 0; i < trueOrderings.size(); i++) {
			IDyadRankingInstance actual = trueOrderings.get(i);
			IDyadRankingInstance predicted = predictedOrderings.get(i);
			avgLoss += lossFunction.loss(actual, predicted);
		}

		avgLoss /= trueOrderings.size();

		return avgLoss;
	}

	/**
	 * Computes the average loss over several dyad orderings. Predictions are
	 * obtained by the given {@link IDyadRanker}.
	 * 
	 * @param lossFunction
	 *            The loss function to be used for the individual
	 *            {@link IDyadRankingInstance}s
	 * @param trueOrderings
	 *            The true orderings represented by {@link IDyadRankingInstance}s
	 * @param ranker
	 *            The {@link IDyadRanker} used to make predictions
	 * @return Average loss over all {@link IDyadRankingInstance}s
	 */
	public static double computeAverageLoss(DyadRankingLossFunction lossFunction, DyadRankingDataset trueOrderings,
			IDyadRanker ranker, Random random) throws PredictionException {
		double avgLoss = 0.0d;
		for (int i = 0; i < trueOrderings.size(); i++) {
			IDyadRankingInstance actual = trueOrderings.get(i);

			// shuffle the instance such that a ranker that doesn't do anything can't come
			// up with a perfect result
			List<Dyad> shuffleContainer = Lists.newArrayList(actual.iterator());
			Collections.shuffle(shuffleContainer, random);
			IDyadRankingInstance shuffledActual = new DyadRankingInstance(shuffleContainer);
			IDyadRankingInstance predicted = ranker.predict(shuffledActual);
			avgLoss += lossFunction.loss(actual, predicted);
		}

		avgLoss /= trueOrderings.size();

		return avgLoss;
	}

	public static double computeAverageLoss(DyadRankingLossFunction lossFunction, DyadRankingDataset trueOrderings,
			IDyadRanker ranker) throws PredictionException {
		return computeAverageLoss(lossFunction, trueOrderings, ranker, new Random(0));
	}
}
