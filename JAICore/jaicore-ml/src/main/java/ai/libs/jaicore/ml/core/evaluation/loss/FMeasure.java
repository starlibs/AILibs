package ai.libs.jaicore.ml.core.evaluation.loss;

public class FMeasure extends AInstanceMeasure<int[]> {

	private final double beta;
	private final Precision precision;
	private final Recall recall;

	public FMeasure(final double beta, final int positiveClass) {
		this.beta = beta;
		this.precision = new Precision(positiveClass);
		this.recall = new Recall(positiveClass);
	}

	@Override
	public double score(final int[] expected, final int[] actual) {
		if (expected.length != actual.length) {
			throw new IllegalArgumentException("Expected and actual must be of the same length.");
		}

		double precision = this.precision.score(expected, actual);
		double recall = this.recall.score(expected, actual);
		double denominator = ((Math.pow(this.beta, 2) * precision) + recall);
		return denominator == 0.0 ? 0 : (1 + Math.pow(this.beta, 2)) * (precision * recall) / denominator;
	}

}
