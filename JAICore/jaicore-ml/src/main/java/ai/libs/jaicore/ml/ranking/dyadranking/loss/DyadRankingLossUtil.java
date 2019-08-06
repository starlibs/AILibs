package ai.libs.jaicore.ml.ranking.dyadranking.loss;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.IRanking;
import org.api4.java.ai.ml.learner.predict.IPrediction;
import org.api4.java.ai.ml.learner.predict.IPredictionBatch;
import org.api4.java.ai.ml.learner.predict.PredictionException;

import com.google.common.collect.Lists;

import ai.libs.jaicore.ml.ranking.dyadranking.Dyad;
import ai.libs.jaicore.ml.ranking.dyadranking.algorithm.IDyadRanker;
import ai.libs.jaicore.ml.ranking.dyadranking.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyadranking.dataset.DyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyadranking.dataset.IDyadRankingInstance;

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
	public static double computeAverageLoss(final IDyadRankingLossFunction lossFunction, final DyadRankingDataset trueOrderings, final IPredictionBatch<IRanking<Dyad>> predictedOrderings) {
		if (trueOrderings.size() != predictedOrderings.getNumPredictions()) {
			throw new IllegalArgumentException("The list of predictions and the list of ground truth dyad rankings need to have the same length!");
		}
		double avgLoss = 0.0d;
		for (int i = 0; i < trueOrderings.size(); i++) {
			IRanking<Dyad> expected = trueOrderings.get(i).getLabel();
			IRanking<Dyad> actual = predictedOrderings.get(i).getPrediction();
			avgLoss += lossFunction.loss(expected, actual);
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
	 * @throws InterruptedException
	 */
	public static <C> double computeAverageLoss(final IDyadRankingLossFunction lossFunction, final DyadRankingDataset trueOrderings, final IDyadRanker<C> ranker, final Random random) throws PredictionException, InterruptedException {
		double avgLoss = 0.0d;
		for (int i = 0; i < trueOrderings.size(); i++) {
			IRanking<Dyad> expected = trueOrderings.get(i).getLabel();

			// shuffle the instance such that a ranker that doesn't do anything can't come
			// up with a perfect result
			List<Dyad> shuffleContainer = Lists.newArrayList(expected.iterator());
			Collections.shuffle(shuffleContainer, random);
			IDyadRankingInstance shuffledActual = new DyadRankingInstance(shuffleContainer);
			IPrediction<IRanking<Dyad>> predicted = ranker.predict(shuffledActual);
			avgLoss += lossFunction.loss(expected, predicted.getPrediction());
		}

		avgLoss /= trueOrderings.size();

		return avgLoss;
	}

	public static <C> double computeAverageLoss(final IDyadRankingLossFunction lossFunction, final DyadRankingDataset trueOrderings, final IDyadRanker<C> ranker) throws PredictionException, InterruptedException {
		return computeAverageLoss(lossFunction, trueOrderings, ranker, new Random(0));
	}
}
