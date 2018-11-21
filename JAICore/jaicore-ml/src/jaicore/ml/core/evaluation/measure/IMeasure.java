package jaicore.ml.core.evaluation.measure;

import java.util.List;

import jaicore.basic.aggregate.IAggregateFunction;

public interface IMeasure<INPUT, OUTPUT> {

	public OUTPUT calculateMeasure(INPUT actual, INPUT expected);

	public List<OUTPUT> calculateMeasure(List<INPUT> actual, List<INPUT> expected);

	public OUTPUT calculateMeasure(List<INPUT> actual, List<INPUT> expected, IAggregateFunction<OUTPUT> aggregateFunction);

	public OUTPUT calculateAvgMeasure(List<INPUT> actual, List<INPUT> expected);

}
