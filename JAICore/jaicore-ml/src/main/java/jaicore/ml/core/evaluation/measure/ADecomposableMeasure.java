package jaicore.ml.core.evaluation.measure;

import java.util.ArrayList;
import java.util.List;

import jaicore.basic.aggregate.IAggregateFunction;

/**
 * A measure that is aggregated from e.g. instance-wise computations of the respective measure and which is then aggregated, e.g. taking the mean.
 *
 * @author mwever
 *
 * @param <I> The type of the inputs.
 * @param <O> The type of the measurement result.
 */

public abstract class ADecomposableMeasure<I, O> implements IMeasure<I, O> {

	@Override
	public List<O> calculateMeasure(final List<I> actual, final List<I> expected) {
		if (expected.size() != actual.size()) {
			throw new IllegalArgumentException("Actual and expected valued need to be of the same size.");
		}
		List<O> deviations = new ArrayList<>(actual.size());
		for (int i = 0; i < actual.size(); i++) {
			deviations.add(this.calculateMeasure(actual.get(i), expected.get(i)));
		}
		return deviations;
	}

	@Override
	public O calculateMeasure(final List<I> actual, final List<I> expected, final IAggregateFunction<O> aggregateFunction) {
		return aggregateFunction.aggregate(this.calculateMeasure(actual, expected));
	}
}
