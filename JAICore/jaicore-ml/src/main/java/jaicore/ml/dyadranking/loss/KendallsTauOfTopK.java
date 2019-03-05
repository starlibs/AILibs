package jaicore.ml.dyadranking.loss;

import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Calculates the kendalls-tau loss only for the top k dyads.
 * 
 * 
 * @author  Mirko Jürgens
 *
 */
public class KendallsTauOfTopK implements DyadRankingLossFunction {
	private int k;
	
	public KendallsTauOfTopK(int k) {
		this.k = k;
	}
	
	@Override
	public double loss(IDyadRankingInstance actual, IDyadRankingInstance predicted) {
			
		int dyadRankingLength = k ;
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
				Dyad predPairedDyad = predicted.getDyadAtPosition(i);
				boolean found = false;
				for (int j = actualIndex + 1; j < dyadRankingLength && !found; j++) {
					if (actual.getDyadAtPosition(j).equals(predPairedDyad)) {
						found = true;
					}
				}
				if (found) {
					nConc++;
				} else {
					nDisc++;
				}
				
			}
		}
		double kendallTau = 2.0 * (nConc - nDisc) / (dyadRankingLength * (dyadRankingLength - 1) );
	
		return kendallTau;
	}
}
