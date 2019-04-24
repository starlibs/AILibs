package jaicore.ml.core.evaluation.measure.singlelabel;

import java.util.List;

import jaicore.basic.aggregate.IAggregateFunction;

public class MeanSquaredErrorLoss extends ASquaredErrorLoss {

	/**
	 * Automatically generated version UID for serialization.
	 */
	private static final long serialVersionUID = 8167100759563769537L;

	@Override
	public Double calculateMeasure(final List<Double> actual, final List<Double> expected, final IAggregateFunction<Double> aggregateFunction) {
		throw new UnsupportedOperationException("This loss can only be accessed via calculateAvgMeasure");
	}
}
