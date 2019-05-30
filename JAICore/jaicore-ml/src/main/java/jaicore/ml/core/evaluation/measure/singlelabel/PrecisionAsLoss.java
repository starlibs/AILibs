package jaicore.ml.core.evaluation.measure.singlelabel;

import java.util.List;

import jaicore.basic.aggregate.IAggregateFunction;
import jaicore.ml.core.evaluation.measure.IMeasure;

public class PrecisionAsLoss implements IMeasure<Double, Double> {

	private final int positiveClass;

	public PrecisionAsLoss(final int positiveClass) {
		this.positiveClass = positiveClass;
	}

	@Override
	public Double calculateMeasure(final Double actual, final Double expected) {
		throw new UnsupportedOperationException("Precision is a batch loss function.");
	}

	@Override
	public List<Double> calculateMeasure(final List<Double> actual, final List<Double> expected) {
		throw new UnsupportedOperationException("Precision is a batch loss function.");
	}

	@Override
	public Double calculateMeasure(final List<Double> actual, final List<Double> expected, final IAggregateFunction<Double> aggregateFunction) {
		throw new UnsupportedOperationException("Precision is a batch loss function.");
	}

	@Override
	public Double calculateAvgMeasure(final List<Double> actual, final List<Double> expected) {
		int tp = 0;
		int fp = 0;

		for (int i = 0; i < actual.size(); i++) {
			int actualValue = (int) (double) actual.get(i);
			int expectedValue = (int) (double) expected.get(i);

			if (actualValue == this.positiveClass) {
				if (actualValue == expectedValue) {
					tp++;
				} else {
					fp++;
				}
			}
		}

		double precision;
		if (tp + fp > 0) {
			precision = (double) tp / (tp + fp);
		} else {
			precision = 0;
		}

		return 1 - precision;
	}

}
