package jaicore.basic.aggregate.reals;

import java.util.List;

public class Mean implements IRealsAggregateFunction {

	@Override
	public Double aggregate(final List<Double> values) {
		return values.stream().mapToDouble(x -> x).average().getAsDouble();
	}

}
