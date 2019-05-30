package jaicore.basic.aggregate.reals;

import java.util.List;

/**
 * The aggregation function "Min" aggregates the given values with the minimum operator, thus, returning the minimum of a list of values.
 *
 * @author mwever
 */
public class Min implements IRealsAggregateFunction {

	@Override
	public Double aggregate(final List<Double> values) {
		return values.stream().mapToDouble(x -> x).min().getAsDouble();
	}
}
