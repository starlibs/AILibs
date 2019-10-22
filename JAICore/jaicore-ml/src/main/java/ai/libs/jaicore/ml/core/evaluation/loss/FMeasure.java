package ai.libs.jaicore.ml.core.evaluation.loss;

public class FMeasure {

	private final double beta;
	private int positiveClass;

	public FMeasure(final double beta, final int positiveClass) {
		this.beta = beta;
	}

	public double loss(final int[] expected, final int[] actual) {
		return 1 - this.score(expected, actual);
	}

	public double score(final int[] expected, final int[] actual) {
		if (expected.length != actual.length) {
			throw new IllegalArgumentException("Expected and actual must be of the same length.");
		}

		int tp = 0;
		int fp = 0;
		int fn = 0;

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] == actual[i]) {
				if (actual[i] == this.positiveClass) {
					tp++;
				}
			} else {
				if (actual[i] == this.positiveClass) {
					fp++;
				} else if (expected[i] == this.positiveClass) {
					fn++;
				}
			}
		}

		double precision = (double) tp / (tp + fp);
		double recall = (double) tp / (tp + fn);
		return (1 + Math.pow(this.beta, 2)) * (precision * recall) / (Math.pow(this.beta, 2) * precision + recall);
	}

}
