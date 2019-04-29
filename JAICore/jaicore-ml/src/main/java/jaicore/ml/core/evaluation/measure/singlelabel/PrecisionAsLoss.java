package jaicore.ml.core.evaluation.measure.singlelabel;

import java.util.List;
import java.util.stream.IntStream;

import jaicore.basic.aggregate.IAggregateFunction;
import jaicore.ml.core.evaluation.measure.IMeasure;

/**
 * Computes the precision measure in calculateAvgMeasure and returns 1-precision as a loss.
 *
 * @author mwever
 */
public class PrecisionAsLoss implements IMeasure<Double, Double> {

	private final Double positiveClass;

	public PrecisionAsLoss(final Double positiveClass) {
		this.positiveClass = positiveClass;
	}

	@Override
	public Double calculateMeasure(final Double actual, final Double expected) {
		throw new UnsupportedOperationException("Precision can only be assessed via calculateAvgMeasure");
	}

	@Override
	public List<Double> calculateMeasure(final List<Double> actual, final List<Double> expected) {
		throw new UnsupportedOperationException("Precision can only be assessed via calculateAvgMeasure");
	}

	@Override
	public Double calculateMeasure(final List<Double> actual, final List<Double> expected, final IAggregateFunction<Double> aggregateFunction) {
		throw new UnsupportedOperationException("Precision can only be assessed via calculateAvgMeasure");
	}

	@Override
	public Double calculateAvgMeasure(final List<Double> actual, final List<Double> expected) {
		if (actual.size() != expected.size()) {
			throw new IllegalArgumentException("Actual and expected must be of the same size.");
		}

		int intersection = IntStream.range(0, actual.size()).filter(x -> actual.get(x) == this.positiveClass && expected.get(x) == this.positiveClass).map(x -> 1).sum();
		int predicted = (int) actual.stream().filter(this.positiveClass::equals).count();

		if (predicted == 0) {
			return 0.0;
		}

		System.out.println("Precision computed to be " + ((double) intersection / predicted));
		return 1.0 - (double) intersection / predicted;
	}

}
