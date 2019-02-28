package jaicore.ml.core.evaluation.measure;

import java.util.ArrayList;
import java.util.List;

import jaicore.basic.aggregate.IAggregateFunction;

public abstract class ADecomposableMeasure<INPUT, OUTPUT> implements IMeasure<INPUT, OUTPUT> {

	@Override
	public List<OUTPUT> calculateMeasure(List<INPUT> actual, List<INPUT> expected) {
		int actualSize = actual.size();
		assert actualSize == expected.size() : "Observing " + actualSize + " actual values and " + expected.size() + " expected values, but the two vectors must coincide in length!";
		List<OUTPUT> deviations = new ArrayList<>(actualSize);
		for (int i = 0; i < actualSize; i++) {
			deviations.add(calculateMeasure(actual.get(i), expected.get(i)));
		}
		return deviations;
	}

	@Override
	public OUTPUT calculateMeasure(List<INPUT> actual, List<INPUT> expected, IAggregateFunction<OUTPUT> aggregateFunction) {
		return aggregateFunction.aggregate(calculateMeasure(actual, expected));
	}
}
