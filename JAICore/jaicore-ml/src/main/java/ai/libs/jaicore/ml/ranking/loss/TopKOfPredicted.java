package ai.libs.jaicore.ml.ranking.loss;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.loss.IRankingPredictionPerformanceMeasure;

/**
 * Calculates if the top-k dyads of the predicted ranking match the top-k dyads
 * of the actual ranking. This ignores the rankings.
 *
 * @author mirkoj
 * @author mwever
 *
 */
public class TopKOfPredicted extends ARankingPredictionPerformanceMeasure implements IRankingPredictionPerformanceMeasure {

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
	public double loss(final IRanking<?> actual, final IRanking<?> predicted) {
		List<Object> topKDyads = new ArrayList<>();
		// first derive the top k ranked dyads
		for (int i = 0; i < this.k; i++) {
			topKDyads.add(actual.get(i));
		}
		int incorrectNum = 0;
		for (int i = 0; i < this.k; i++) {
			Object topKDyadInPred = predicted.get(i);
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
