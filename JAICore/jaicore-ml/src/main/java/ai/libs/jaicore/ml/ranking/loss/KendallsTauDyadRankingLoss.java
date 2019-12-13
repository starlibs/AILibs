package ai.libs.jaicore.ml.ranking.loss;

import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.loss.IRankingPredictionPerformanceMeasure;

/**
 * Computes the rank correlation measure known as Kendall's tau coefficient, i.e.
 * (C - D) / (K * (K-1) /2), where C and D are the number of concordant (put in the right order)
 * and discordant (put in the wrong order) pairs of dyads and K is the length of the dyad ranking.
 * Lies between -1 (reversed order) and +1 (same order).
 * Assumes the dyads in the ranking to be pairwise distinct.
 *
 * @author mbraun
 * @author mwever
 *
 */

public class KendallsTauDyadRankingLoss extends ARankingPredictionPerformanceMeasure implements IRankingPredictionPerformanceMeasure {

	@Override
	public double loss(final IRanking<?> expected, final IRanking<?> predicted) {
		int dyadRankingLength = expected.size();
		if (dyadRankingLength <= 1) {
			throw new IllegalArgumentException("Dyad rankings must have length greater than 1.");
		}
		int nConc = 0;
		int nDisc = 0;

		for (int predIndex = 0; predIndex < dyadRankingLength - 1; predIndex++) {
			Object predDyad = predicted.get(predIndex);
			int actualIndex = -1;
			for (int i = 0; i < dyadRankingLength; i++) {
				if (expected.get(i).equals(predDyad)) {
					actualIndex = i;
					break;
				}
			}

			for (int i = predIndex + 1; i < dyadRankingLength; i++) {
				if (this.isRankingCorrectForIndex(expected, predicted, dyadRankingLength, actualIndex, i)) {
					nConc++;
				} else {
					nDisc++;
				}

			}
		}
		return 2.0 * (nConc - nDisc) / (dyadRankingLength * (dyadRankingLength - 1));
	}

	private boolean isRankingCorrectForIndex(final IRanking<?> actual, final IRanking<?> predicted, final int dyadRankingLength, final int actualIndex, final int i) {
		Object predPairedDyad = predicted.get(i);
		boolean found = false;
		for (int j = actualIndex + 1; j < dyadRankingLength && !found; j++) {
			if (actual.get(j).equals(predPairedDyad)) {
				found = true;
			}
		}
		return found;
	}

}
