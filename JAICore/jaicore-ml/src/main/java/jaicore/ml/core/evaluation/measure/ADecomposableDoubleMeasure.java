package jaicore.ml.core.evaluation.measure;

import java.util.List;

import jaicore.basic.aggregate.reals.Mean;

public abstract class ADecomposableDoubleMeasure<INPUT> extends ADecomposableMeasure<INPUT, Double> {

	@Override
	public Double calculateAvgMeasure(final List<INPUT> actual, final List<INPUT> expected) {
		return this.calculateMeasure(actual, expected, new Mean());
	}
}
