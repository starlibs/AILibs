package jaicore.ml.dyadranking.loss;

import java.util.HashMap;
import java.util.Map;

import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * The Normalized Discounted Cumulative Gain for ranking.
 * @author Michael Braun
 *
 */
public class NDCGLoss implements DyadRankingLossFunction {

	@Override
	public double loss(IDyadRankingInstance actual, IDyadRankingInstance predicted) {	
		int L = actual.length();
		if (L <= 1) {
			throw new IllegalArgumentException("Dyad rankings must have length greater than 1.");
		}
		
		Map<Dyad, Integer> relevance = new HashMap<Dyad, Integer>();
		for (int i = 0; i < L; i++) {
			relevance.put(actual.getDyadAtPosition(i), -(i+1));
		}
		
		double DCG = computeDCG(predicted, relevance);
		double IDCG = computeDCG(actual, relevance);
		
		return DCG / IDCG;
	}
	
	private double computeDCG(IDyadRankingInstance ranking, Map<Dyad, Integer> relevance) {
		int L = ranking.length();
		double DCG = 0;
		for (int i = 0; i < L; i++) {
			DCG += (Math.pow(2, relevance.get(ranking.getDyadAtPosition(i))) - 1) / log2(i + 2);
		}
		return DCG;
	}
	
	private double log2(double x) {
		return Math.log(x) / Math.log(2);
	}
	
	
}
