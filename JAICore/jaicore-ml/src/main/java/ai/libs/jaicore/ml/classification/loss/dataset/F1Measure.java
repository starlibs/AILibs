package ai.libs.jaicore.ml.classification.loss.dataset;

public class F1Measure extends FMeasure {

	private static final int BETA = 1;

	public F1Measure(final int positiveClass) {
		super(BETA, positiveClass);
	}

}
