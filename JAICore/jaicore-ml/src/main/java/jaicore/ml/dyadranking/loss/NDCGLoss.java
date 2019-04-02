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
	
	/**
	 * The position up to which to compute the cumulative gain (zero-indexed, exclusive). 
	 */
	private int l;
	
	/**
	 * 
	 * @param l The position up to which to compute the cumulative gain (zero-indexed, exclusive). 
	 */
	public NDCGLoss(int l) {
		super();
		this.setL(l);
	}

	@Override
	public double loss(IDyadRankingInstance actual, IDyadRankingInstance predicted) {	
		if (actual.length() <= 1) {
			throw new IllegalArgumentException("Dyad rankings must have length greater than 1.");
		}
		
		if (actual.length() != predicted.length()) {
			throw new IllegalArgumentException("Dyad rankings must have equal length.");
		}
		
		Map<Dyad, Integer> relevance = new HashMap<>();
		for (int i = 0; i < l; i++) {
			relevance.put(actual.getDyadAtPosition(i), -(i+1));
		}
		
		double dcg = computeDCG(predicted, relevance);
		double idcg = computeDCG(actual, relevance);
		
		if (dcg != 0) {
			return idcg / dcg;
		} else {
			return 0;
		}
	}
	
	private double computeDCG(IDyadRankingInstance ranking, Map<Dyad, Integer> relevance) {
		int length = ranking.length();
		double dcg = 0;
		for (int i = 0; i < length; i++) {
			dcg += (Math.pow(2, relevance.get(ranking.getDyadAtPosition(i))) - 1) / log2(i + 2.0);
		}
		return dcg;
	}
	
	private double log2(double x) {
		return Math.log(x) / Math.log(2);
	}

	public int getL() {
		return l;
	}

	public void setL(int l) {
		this.l = l;
	}
	
	
}
