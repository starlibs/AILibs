package ai.libs.jaicore.ml.classification.loss.instance;

public class LogLoss extends AInstanceMeasure<Integer, double[]> {

	private static final double DEF_EPSILON = 1E-15;

	private final double epsilon;

	public LogLoss() {
		this(DEF_EPSILON);
	}

	public LogLoss(final double epsilon) {
		this.epsilon = epsilon;
	}

	@Override
	public double loss(final Integer expected, final double[] actual) {
		for (int i = 0; i < actual.length; i++) {
			if (expected == i) {
				double minMax = Math.min(1 - this.epsilon, Math.max(this.epsilon, actual[i]));
				return -Math.log(minMax);
			}
		}
		throw new IllegalArgumentException("Expected class index not in range of the probability vector");
	}

}
