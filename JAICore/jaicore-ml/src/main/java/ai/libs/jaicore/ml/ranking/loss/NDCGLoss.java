package ai.libs.jaicore.ml.ranking.loss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.loss.IRankingPredictionPerformanceMeasure;

/**
 * The Normalized Discounted Cumulative Gain for ranking.
 *
 * @author mbraun
 * @author mwever
 *
 */
public class NDCGLoss extends ARankingPredictionPerformanceMeasure implements IRankingPredictionPerformanceMeasure {

	/**
	 * The position up to which to compute the cumulative gain (zero-indexed, exclusive).
	 */
	private int l;

	/**
	 *
	 * @param l The position up to which to compute the cumulative gain (zero-indexed, exclusive).
	 */
	public NDCGLoss(final int l) {
		super();
		this.setL(l);
	}

	@Override
	public double loss(final List<? extends IRanking<?>> expected, final List<? extends IRanking<?>> actual) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> this.loss(expected.get(0), actual.get(0))).average();
		if (res.isPresent()) {
			return res.getAsDouble();
		}
		throw new IllegalStateException("Could not aggregate kendalls tau of top k");
	}

	@Override
	public double loss(final IRanking<?> expected, final IRanking<?> actual) {
		if (expected.size() <= 1) {
			throw new IllegalArgumentException("Dyad rankings must have length greater than 1.");
		}

		if (expected.size() != actual.size()) {
			throw new IllegalArgumentException("Dyad rankings must have equal length.");
		}

		Map<Object, Integer> relevance = new HashMap<>();
		for (int i = 0; i < this.l; i++) {
			relevance.put(expected.get(i), -(i + 1));
		}

		double dcg = this.computeDCG(actual, relevance);
		double idcg = this.computeDCG(expected, relevance);

		if (dcg != 0) {
			return idcg / dcg;
		}
		return 0;
	}

	private double computeDCG(final IRanking<?> ranking, final Map<Object, Integer> relevance) {
		int length = ranking.size();
		double dcg = 0;
		for (int i = 0; i < length; i++) {
			dcg += (Math.pow(2, relevance.get(ranking.get(i))) - 1) / this.log2(i + 2.0);
		}
		return dcg;
	}

	private double log2(final double x) {
		return Math.log(x) / Math.log(2);
	}

	public int getL() {
		return this.l;
	}

	public void setL(final int l) {
		this.l = l;
	}

}
