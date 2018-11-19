package jaicore.ml.metric;

import java.util.List;

import jaicore.basic.aggregate.IAggregateFunction;

public abstract class AMetric<INPUT, OUTPUT> {

	public abstract OUTPUT calculateMetric(INPUT actual, INPUT expected);

	public abstract OUTPUT calculateMetric(List<INPUT> actual, List<INPUT> expected);

	public abstract OUTPUT calculateMetric(List<INPUT> actual, List<INPUT> expected, IAggregateFunction<OUTPUT> aggregateFunction);

}
