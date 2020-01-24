package ai.libs.jaicore.ml.ranking.dyad.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.common.math.IVector;

import ai.libs.jaicore.ml.ranking.dyad.dataset.DenseDyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.learner.Dyad;

/**
 * Creates simple rankings for testing purposes.
 *
 * @author Jonas Hanselle, Mirko Jürgens, Helena Graf, Michael Braun
 *
 */
public class DyadRankingInstanceSupplier {

	private DyadRankingInstanceSupplier() {
		// Intentionally left blank
	}

	/**
	 * Creates a random {@link ai.libs.jaicore.ml.ranking.dyad.dataset.DenseDyadRankingInstance}
	 * consisting of (with 2 alternatives and 2 instances)
	 *
	 * @param maxLength The amount of dyads
	 * @param seed Seed for generating random dyads
	 * @return random dyad ranking instance of length at most maxLength
	 */
	public static DenseDyadRankingInstance getDyadRankingInstance(final int maxLength, final int seed) {
		List<IDyad> dyads = new ArrayList<>();
		if (maxLength <= 1) {
			throw new IllegalArgumentException("Length must be at least 2.");
		}
		Random random = new Random(seed);
		int actualLength = random.nextInt(maxLength + 1);
		while (actualLength == 0) {
			actualLength = random.nextInt(maxLength + 1);
		}
		for (int i = 0; i < actualLength; i++) {
			Dyad dyad = DyadSupplier.getRandomDyad(i, 2, 2);
			dyads.add(dyad);
		}
		Comparator<IDyad> comparator = complexDyadRanker();
		Collections.sort(dyads, comparator);
		return new DenseDyadRankingInstance(dyads);
	}

	/**
	 * Creates a comparator for {@link ai.libs.jaicore.ml.ranking.dyad.learner.Dyad} (with 2
	 * instances x_1, x_2 and 2 alternatives y_1,y_2). A pair of dyads (d_i, d_j) is
	 * then ranked by the rule d_i >= d_j iff x_i1^2 + x_i2^2 - y_i1^2 - y_i2^2 >
	 * x_j1^2 + x_j2^2 - y_j1^2 - y_j2^2
	 *
	 * @return Comparator for dyads with 2 instances and 2 alternatives
	 */
	public static Comparator<IDyad> complexDyadRanker() {
		return (final IDyad d1, final IDyad d2) -> {
			IVector scoreVecI = d1.getContext();
			IVector scoreVecA = d1.getAlternative();
			IVector scoreVecI2 = d2.getContext();
			IVector scoreVecA2 = d2.getAlternative();
			double score1 = bilinFunc(scoreVecI, scoreVecA);
			double score2 = bilinFunc(scoreVecI2, scoreVecA2);
			double scoreDiff = score1 - score2;
			int comparison = scoreDiff > 0 ? 1 : -1;
			return score1 - score2 == 0 ? 0 : (comparison);
		};
	}

	/**
	 * A simple function that can be learned by a bilinear feature transform:
	 * <code>
	 * f((x_1,y_1) , (x_2, y_2)) = x1*y1 + x2*y2 + x1*y2 + x2*y1
	 * </code>
	 *
	 * @param scoreVec1 (x_1, y_1)
	 * @param scoreVec2 (x_2, y_2)
	 * @return
	 */
	private static final double bilinFunc(final IVector scoreVec1, final IVector scoreVec2) {
		double score = scoreVec1.getValue(0) * scoreVec2.getValue(0) + scoreVec1.getValue(1) * scoreVec2.getValue(1) + scoreVec1.getValue(0) * scoreVec2.getValue(1) + scoreVec1.getValue(1) * scoreVec2.getValue(0);
		return Math.exp(score);
	}

	/**
	 *
	 * @param maxLengthDyadRankingInstance
	 *            Maximum length of an individual dyad ranking instance in the
	 *            dataset
	 * @param size
	 *            Number of dyad ranking instances in the dataset
	 * @return A dyad ranking dataset with random dyads ((with 2 alternatives and 2
	 *         instances) that are ranked by the ranking function implemented by the
	 *         {@link Comparator} returned by {@link #complexDyadRanker()}
	 */
	public static DyadRankingDataset getDyadRankingDataset(final int maxLengthDyadRankingInstance, final int length) {
		DyadRankingDataset dataset = new DyadRankingDataset();
		for (int i = 0; i < length; i++) {
			dataset.add(DyadRankingInstanceSupplier.getDyadRankingInstance(maxLengthDyadRankingInstance, i));
		}
		return dataset;
	}

	public static double inputOptimizerTestScore(final IDyad dyad) {
		IVector inst = dyad.getContext();
		IVector alt = dyad.getAlternative();
		return Math.abs(inst.getValue(0) + inst.getValue(1) - alt.getValue(0) - alt.getValue(1));
	}

	public static Comparator<IDyad> inputOptimizerTestRanker() {
		return (final IDyad d1, final IDyad d2) -> {
			double score1 = inputOptimizerTestScore(d1);
			double score2 = inputOptimizerTestScore(d2);
			int comparison = score1 - score2 > 0 ? 1 : -1;
			return score1 - score2 == 0 ? 0 : comparison;
		};
	}

	public static DenseDyadRankingInstance getInputOptDyadRankingInstance(final int maxLength) {
		List<IDyad> dyads = new ArrayList<>();
		int actualLength = ThreadLocalRandom.current().nextInt(2, maxLength + 1);

		for (int i = 0; i < actualLength; i++) {
			Dyad dyad = DyadSupplier.getRandomDyad(2, 2);
			dyads.add(dyad);
		}
		Comparator<IDyad> comparator = inputOptimizerTestRanker();
		Collections.sort(dyads, comparator);
		return new DenseDyadRankingInstance(dyads);
	}

	public static DyadRankingDataset getInputOptTestSet(final int maxLengthDyadRankingInstance, final int size) {
		DyadRankingDataset dataset = new DyadRankingDataset();
		for (int i = 0; i < size; i++) {
			dataset.add(DyadRankingInstanceSupplier.getInputOptDyadRankingInstance(maxLengthDyadRankingInstance));
		}
		return dataset;
	}
}