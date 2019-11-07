package ai.libs.jaicore.ml.core.evaluation.loss;

public class Precision extends AInstanceMeasure<int[]> {

	private final TruePositives tp;
	private final FalsePositives fp;

	public Precision(final int positiveClass) {
		this.tp = new TruePositives(positiveClass);
		this.fp = new FalsePositives(positiveClass);
	}

	@Override
	public double score(final int[] expected, final int[] actual) {
		double truePositives = this.tp.score(expected, actual);
		return truePositives / (truePositives + this.fp.score(expected, actual));
	}

}
