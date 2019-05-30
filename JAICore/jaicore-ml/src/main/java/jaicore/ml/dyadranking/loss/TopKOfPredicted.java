package jaicore.ml.dyadranking.loss;

import java.util.ArrayList;
import java.util.List;

import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Calculates if the top-k dyads of the predicted ranking match the top-k dyads
 * of the actual ranking. This ignores the rankings.
 *
 * @author Mirko Jürgens
 *
 */
public class TopKOfPredicted implements DyadRankingLossFunction {

	private int k;

	/**
	 * Specifies the amount of top rankings to consider.
	 *
	 * @param k
	 */
	public TopKOfPredicted(final int k) {
		this.k = k;
	}

	@Override
	public double loss(final IDyadRankingInstance actual, final IDyadRankingInstance predicted) {
		List<Dyad> topKDyads = new ArrayList<>();
		// first derive the top k ranked dyads
		for (int i = 0; i < this.k; i++) {
			topKDyads.add(actual.getDyadAtPosition(i));
		}
		int incorrectNum = 0;
		for (int i = 0; i < this.k; i++) {
			Dyad topKDyadInPred = predicted.getDyadAtPosition(i);
			if (!topKDyads.contains(topKDyadInPred)) {
				incorrectNum++;
			}
		}
		if (incorrectNum == 0) {
			return 0.0d;
		}
		return ((double) incorrectNum / (double) this.k);
	}

}
