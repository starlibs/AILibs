package jaicore.basic.aggregate.reals;

import java.util.List;

/**
 * The aggregation function "Mean" aggregates the given values with the mean operator, thus, returning the average of a list of values.
 *
 * @author mwever
 */
public class Mean implements IRealsAggregateFunction {

	@Override
	public Double aggregate(final List<Double> values) {
		return values.stream().mapToDouble(x -> x).average().getAsDouble();
	}

}
