package ai.libs.jaicore.ml.core.evaluation.loss;

public class Precision extends AInstanceMeasure<int[], int[]> {

	private final TruePositives tp;
	private final FalsePositives fp;

	public Precision(final int positiveClass) {
		this.tp = new TruePositives(positiveClass);
		this.fp = new FalsePositives(positiveClass);
	}

	@Override
	public double score(final int[] expected, final int[] actual) {
		double truePositives = this.tp.score(expected, actual);
		double denominator = (truePositives + this.fp.score(expected, actual));
		return denominator == 0.0 ? 0 : truePositives / denominator;
	}

}
