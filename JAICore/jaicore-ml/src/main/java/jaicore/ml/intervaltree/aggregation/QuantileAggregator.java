package jaicore.ml.intervaltree.aggregation;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

/**
 * A {@link IntervalAggregator} that works based on quantiles. That is, if a
 * quantile with value 0.15 is chosen, the aggregator would choose the 0.85
 * quantile of the predictions as the maximum and the 0.15 quantile as the
 * minimum.
 *
 * @author elppa
 *
 */
public class QuantileAggregator implements IntervalAggregator {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = -6765279710955694443L;

	private final Percentile maxQuantile;

	private final Percentile minQuantile;

	public QuantileAggregator(final double quantile) {
		if (quantile < 0 || quantile > 1) {
			throw new IllegalArgumentException("Quantile values have to be in [0, 1]");
		}
		this.maxQuantile = new Percentile(1 - quantile);
		this.minQuantile = new Percentile(quantile);
	}

	@Override
	public Interval aggregate(final List<Double> toAggregate) {
		// since Double is a wrapper type we have to copy manually :/
		double[] mappedValues = new double[toAggregate.size()];
		for (int i = 0; i < toAggregate.size(); i++) {
			mappedValues[i] = toAggregate.get(i);
		}
		double min = this.minQuantile.evaluate(mappedValues);
		double max = this.maxQuantile.evaluate(mappedValues);
		return new Interval(min, max);
	}
}
