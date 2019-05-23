package jaicore.ml.intervaltree.aggregation;

import java.util.List;

import jaicore.ml.core.Interval;

/**
 * An {@link IntervalAggregator} that makes predictions using the minimum of the
 * predictions as the lower bound and the maximum as the upper bound. These
 * predictions are, generally speaking, favorable if the correctness property
 * of the RQP is desired over the precision property.
 * 
 * @author elppa
 *
 */
public class AggressiveAggregator implements IntervalAggregator {

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = -1354655063228985606L;

	@Override
	public Interval aggregate(List<Double> toAggregate) {
		double min = toAggregate.stream().mapToDouble(x -> x).min().getAsDouble();
		double max = toAggregate.stream().mapToDouble(x -> x).max().getAsDouble();
		return new Interval(min, max);
	}

}
