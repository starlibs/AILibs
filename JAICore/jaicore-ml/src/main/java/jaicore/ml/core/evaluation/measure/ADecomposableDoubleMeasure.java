package jaicore.ml.core.evaluation.measure;

import java.util.List;

import jaicore.basic.aggregate.reals.Mean;

/**
 * A measure that is decomposable by instances and aggregated by averaging.
 *
 * @author mwever
 *
 * @param <I> The type of the inputs to compute the measure.
 */
public abstract class ADecomposableDoubleMeasure<I> extends ADecomposableMeasure<I, Double> {

	@Override
	public Double calculateAvgMeasure(final List<I> actual, final List<I> expected) {
		return this.calculateMeasure(actual, expected, new Mean());
	}
}
