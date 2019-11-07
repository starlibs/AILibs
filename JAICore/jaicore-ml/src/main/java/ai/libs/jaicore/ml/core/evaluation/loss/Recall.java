package ai.libs.jaicore.ml.core.evaluation.loss;

public class Recall extends AInstanceMeasure<int[]> {

	private final TruePositives tp;
	private final FalseNegatives fn;

	public Recall(final int positiveClass) {
		this.tp = new TruePositives(positiveClass);
		this.fn = new FalseNegatives(positiveClass);
	}

	@Override
	public double score(final int[] expected, final int[] actual) {
		double truePositives = this.tp.score(expected, actual);
		return truePositives / (truePositives + this.fn.score(expected, actual));
	}

}
