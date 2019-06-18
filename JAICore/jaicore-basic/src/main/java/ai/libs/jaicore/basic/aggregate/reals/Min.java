package ai.libs.jaicore.basic.aggregate.reals;

import java.util.List;

public class Min implements IRealsAggregateFunction {

	@Override
	public Double aggregate(final List<Double> values) {
		return values.stream().mapToDouble(x -> x).min().getAsDouble();
	}
}
