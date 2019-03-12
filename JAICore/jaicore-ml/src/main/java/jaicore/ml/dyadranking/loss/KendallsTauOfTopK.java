package jaicore.ml.dyadranking.loss;

import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Calculates the kendalls-tau loss only for the top k dyads.
 * 
 * https://researcher.watson.ibm.com/researcher/files/us-fagin/topk.pdf
 * 
 * @author Mirko Jürgens
 *
 */
public class KendallsTauOfTopK implements DyadRankingLossFunction {
	private int k;

	private double p;

	public KendallsTauOfTopK(int k, double p) {
		this.k = k;
		this.p = p;
	}

	@Override
	public double loss(IDyadRankingInstance actual, IDyadRankingInstance predicted) {

		double kendallsDistance = 0;
		if (k <= 1) {
			throw new IllegalArgumentException("Dyad rankings must have length greater than 1.");
		}
		
		for (int actualI = 0; actualI < actual.length() - 1; actualI++) {
			Dyad actualDyad = actual.getDyadAtPosition(actualI);
			int predictedI = -1;
			for (int i = 0; i < predicted.length(); i++) {
				if (predicted.getDyadAtPosition(i).equals(actualDyad)) {
					predictedI = i;
					break;
				}
			}

			for (int actualJ = actualI + 1; actualJ < actual.length(); actualJ++) {
				Dyad actPairedDyad = actual.getDyadAtPosition(actualJ);
				int predictedJ = -1;
				for (int j = 0; j < predicted.length(); j++) {
					if (predicted.getDyadAtPosition(j).equals(actPairedDyad)) {
						predictedJ = j;
						break;
					}
				}
				

				double penalty = 0;

				boolean iAndJAreBothInPredictedTopK = predictedI < k && predictedJ < k;
				boolean iAndJAreBothInActualTopK = actualI < k && actualJ < k;
				// case 1: i,j are both in the top k list of the predicted and actual ranking
				if (iAndJAreBothInActualTopK && iAndJAreBothInPredictedTopK) {
					// case 1.1: if they are ranked the same in both topk lists: 0 penalty
					boolean iIsBetterThanJInPredictedAndActualRanking = predictedI < predictedJ && actualI < actualJ;
					boolean jIsBetterThanIInPredictedAndActualRanking = predictedI > predictedJ && actualI > actualJ;
					if (iIsBetterThanJInPredictedAndActualRanking || jIsBetterThanIInPredictedAndActualRanking) {
						penalty = 0;
					}
					// case 1.2 ranking mismatch in one of them
					boolean iIsBetterThanJInPredictedButNotInActualRanking = predictedI < predictedJ
							&& actualI > actualJ;
					boolean jIsBetterThanIInPredictedButNotInActualRanking = predictedI > predictedJ
							&& actualI < actualJ;
					if (iIsBetterThanJInPredictedButNotInActualRanking
							|| jIsBetterThanIInPredictedButNotInActualRanking) {
						penalty = 1;
					}
				}
				boolean justIIsInPredictedTopK = predictedI < k && predictedJ >= k;
				boolean justJIsInPredictedTopK = predictedJ < k && predictedI >= k;

				boolean justIIsInActualTopK = actualI < k && actualJ >= k;
				boolean justJIsInActualTopK = actualJ < k && actualI >= k;

				// case 2: i,j are both in one top k ranking but for the other ranking just one
				// is in the top k
				boolean bothPredictedAreInTopKButJustOneActual = (iAndJAreBothInPredictedTopK && justIIsInActualTopK)
						|| (iAndJAreBothInPredictedTopK && justJIsInPredictedTopK);
				boolean bothActualAreInTopKButJustOnePredicted = (iAndJAreBothInActualTopK && justIIsInPredictedTopK)
						|| (iAndJAreBothInActualTopK && justJIsInPredictedTopK);

				if (bothActualAreInTopKButJustOnePredicted) {
					if (actualI < actualJ) {
						// we know that actualI < actualJ < k
						// if just i is in the predicted top k then we know that predictedI < predictedJ
						if (justIIsInPredictedTopK) {
							penalty = 0;
						} else {
							// predictedJ > predictedI
							penalty = 1;
						}
					} else {
						// actualJ < actualI
						// if just j is in the predicted top k the predictedJ < predictedI
						if (justJIsInPredictedTopK) {
							penalty = 0;
						} else {
							penalty = 1;
						}
					}
				}
				if (bothPredictedAreInTopKButJustOneActual) {
					if (predictedI < predictedJ) {
						// again, we know that predictedI < predictedJ < k
						// likewise, if the i of the actual ranking is in top k we are fine
						if (justIIsInActualTopK) {
							penalty = 0;
						} else {
							penalty = 1;
						}
					} else {
						// predictedJ < predictedI < k
						if (justJIsInActualTopK) {
							penalty = 0;
						} else {
							penalty = 1;
						}
					}
				}
				// case 3: i, but not j, appears in one top k list , and j, but not i, appears
				// in the other top k list
				if (justIIsInActualTopK && justJIsInPredictedTopK) {
					penalty = 1;
				}
				if (justJIsInActualTopK && justIIsInPredictedTopK) {
					penalty = 1;
				}

				// case 4:
				boolean neitherIOrJAreInPredictedTopK = predictedI >= k && predictedJ >= k;
				boolean neitherIOrJAreInActualTopK = actualI >= k && actualJ >= k;

				if (iAndJAreBothInActualTopK && neitherIOrJAreInPredictedTopK) {
					penalty = p;
				}
				if (iAndJAreBothInPredictedTopK && neitherIOrJAreInActualTopK) {
					penalty = p;
				}

				kendallsDistance += penalty;
			}
		}
		System.out.println("KD "+ kendallsDistance);

		return kendallsDistance;
	}
}
