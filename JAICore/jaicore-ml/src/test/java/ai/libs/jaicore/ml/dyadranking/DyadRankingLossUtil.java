package ai.libs.jaicore.ml.dyadranking;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.IRankingPrediction;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.ai.ml.ranking.loss.IRankingLossFunction;

import com.google.common.collect.Lists;

import ai.libs.jaicore.ml.ranking.IRankingPredictionBatch;
import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.learner.Dyad;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.IDyadRanker;

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
	public static double computeAverageLoss(final IRankingLossFunction lossFunction, final List<IRanking<Dyad>> trueOrderings, final IRankingPredictionBatch predictedOrderings) {
		if (trueOrderings.size() != predictedOrderings.getNumPredictions()) {
			throw new IllegalArgumentException("The list of predictions and the list of ground truth dyad rankings need to have the same length!");
		}
		double avgLoss = 0.0d;
		for (int i = 0; i < trueOrderings.size(); i++) {
			IRanking<?> expected = trueOrderings.get(i);
			IRanking<?> actual = predictedOrderings.get(i).getPrediction();
			avgLoss += lossFunction.loss(actual, expected);
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
	public static double computeAverageLoss(final IRankingLossFunction lossFunction, final DyadRankingDataset trueOrderings, final IDyadRanker ranker, final Random random) throws PredictionException, InterruptedException {
		double avgLoss = 0.0d;
		for (int i = 0; i < trueOrderings.size(); i++) {
			IRanking<?> expected = trueOrderings.get(i).getLabel();

			// shuffle the instance such that a ranker that doesn't do anything can't come
			// up with a perfect result
			List<Object> shuffleContainer = Lists.newArrayList(expected.iterator());
			Collections.shuffle(shuffleContainer, random);
			IDyadRankingInstance shuffledActual = new DyadRankingInstance(trueOrderings.getInstanceSchema(), shuffleContainer);
			IRankingPrediction predicted = ranker.predict(shuffledActual);
			avgLoss += lossFunction.loss(predicted.getPrediction(), expected);
		}

		avgLoss /= trueOrderings.size();

		return avgLoss;
	}

	public static double computeAverageLoss(final IRankingLossFunction lossFunction, final DyadRankingDataset trueOrderings, final IDyadRanker ranker) throws PredictionException, InterruptedException {
		return computeAverageLoss(lossFunction, trueOrderings, ranker, new Random(0));
	}
}
