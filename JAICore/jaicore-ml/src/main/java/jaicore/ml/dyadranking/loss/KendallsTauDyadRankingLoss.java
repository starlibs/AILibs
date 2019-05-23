package jaicore.ml.dyadranking.loss;

import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Computes the rank correlation measure known as Kendall's tau coefficient, i.e.
 * (C - D) / (K * (K-1) /2), where C and D are the number of concordant (put in the right order)
 * and discordant (put in the wrong order) pairs of dyads and K is the length of the dyad ranking.
 * Lies between -1 (reversed order) and +1 (same order).
 * Assumes the dyads in the ranking to be pairwise distinct.
 * 
 * @author Michael Braun
 *
 */

public class KendallsTauDyadRankingLoss implements DyadRankingLossFunction {

	@Override
	public double loss(IDyadRankingInstance actual, IDyadRankingInstance predicted) {
			
		int dyadRankingLength = actual.length();
		if (dyadRankingLength <= 1) {
			throw new IllegalArgumentException("Dyad rankings must have length greater than 1.");
		}
		int nConc = 0;
		int nDisc = 0;
		
		for (int predIndex = 0; predIndex < dyadRankingLength - 1; predIndex++) {
			Dyad predDyad = predicted.getDyadAtPosition(predIndex);
			int actualIndex = -1;
			for (int i = 0; i < dyadRankingLength; i++) {
				if (actual.getDyadAtPosition(i).equals(predDyad)) {
					actualIndex = i;
					break;
				}
			}
			
			for (int i = predIndex + 1; i < dyadRankingLength; i++) {
				if (isRankingCorrectForIndex(actual, predicted, dyadRankingLength, actualIndex, i)) {
					nConc++;
				} else {
					nDisc++;
				}
				
			}
		}
		
		return 2.0 * (nConc - nDisc) / (dyadRankingLength * (dyadRankingLength - 1) );
	}

	private boolean isRankingCorrectForIndex(IDyadRankingInstance actual, IDyadRankingInstance predicted, int dyadRankingLength,
			int actualIndex, int i) {
		Dyad predPairedDyad = predicted.getDyadAtPosition(i);
		boolean found = false;
		for (int j = actualIndex + 1; j < dyadRankingLength && !found; j++) {
			if (actual.getDyadAtPosition(j).equals(predPairedDyad)) {
				found = true;
			}
		}
		return found;
	}

}
