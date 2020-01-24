package ai.libs.jaicore.ml.ranking.dyad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.ai.ml.ranking.loss.IRankingPredictionPerformanceMeasure;

import com.google.common.collect.Lists;

import ai.libs.jaicore.ml.ranking.dyad.dataset.DenseDyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
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
	public static double computeAverageLoss(final IRankingPredictionPerformanceMeasure lossFunction, final List<IRanking<?>> trueOrderings, final List<IRanking<?>> predictedOrderings) {
		if (trueOrderings.size() != predictedOrderings.size()) {
			throw new IllegalArgumentException("The list of predictions and the list of ground truth dyad rankings need to have the same length!");
		}
		return lossFunction.loss(predictedOrderings, trueOrderings);
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
	public static double computeAverageLoss(final IRankingPredictionPerformanceMeasure lossFunction, final DyadRankingDataset trueOrderings, final IDyadRanker ranker, final Random random) throws PredictionException, InterruptedException {
		List<IRanking<?>> predictedOrderings = new ArrayList<>();
		List<IRanking<?>> expectedOrderings = new ArrayList<>();
		for (int i = 0; i < trueOrderings.size(); i++) {
			IRanking<IDyad> expected = trueOrderings.get(i).getLabel();
			expectedOrderings.add(expected);

			// shuffle the instance such that a ranker that doesn't do anything can't come
			// up with a perfect result
			List<IDyad> shuffleContainer = Lists.newArrayList(expected.iterator());
			Collections.shuffle(shuffleContainer, random);
			IDyadRankingInstance shuffledActual = new DenseDyadRankingInstance(shuffleContainer);
			IRanking<?> predicted = ranker.predict(shuffledActual);
			predictedOrderings.add(predicted);
		}
		return lossFunction.loss(expectedOrderings, predictedOrderings);
	}

	public static double computeAverageLoss(final IRankingPredictionPerformanceMeasure lossFunction, final DyadRankingDataset trueOrderings, final IDyadRanker ranker) throws PredictionException, InterruptedException {
		return computeAverageLoss(lossFunction, trueOrderings, ranker, new Random(0));
	}
}
