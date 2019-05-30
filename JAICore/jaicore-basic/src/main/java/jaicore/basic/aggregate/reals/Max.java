package jaicore.basic.aggregate.reals;

import java.util.List;

/**
 * The aggregation function "Max" aggregates the given values with the maximum operator, thus, returning the maximum of a list of values.
 *
 * @author mwever
 */
public class Max implements IRealsAggregateFunction {

	@Override
	public Double aggregate(final List<Double> values) {
		return values.stream().mapToDouble(x -> x).max().getAsDouble();
	}

}
