package jaicore.basic.aggregate.reals;

import java.util.List;

public class Max implements IRealsAggregateFunction {

	@Override
	public Double aggregate(final List<Double> values) {
		return values.stream().mapToDouble(x -> x).max().getAsDouble();
	}
}
