package ai.libs.jaicore.ml.classification.loss.instance;

import java.util.stream.IntStream;

public class CrossEntropyLoss extends AInstanceMeasure<double[], double[]> {

	public static final double DEF_EPSILON = 1E-15;

	private final double epsilon;

	public CrossEntropyLoss() {
		this(DEF_EPSILON);
	}

	public CrossEntropyLoss(final double epsilon) {
		this.epsilon = epsilon;
	}

	@Override
	public double loss(final double[] expected, final double[] predicted) {
		return -IntStream.range(0, expected.length).mapToDouble(i -> expected[i] * Math.log(this.minMax(predicted[i]))).sum();
	}

	private double minMax(final double value) {
		return Math.min(1 - this.epsilon, Math.max(value, this.epsilon));
	}

}
