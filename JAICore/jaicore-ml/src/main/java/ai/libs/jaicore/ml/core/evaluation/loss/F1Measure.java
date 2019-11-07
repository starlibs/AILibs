package ai.libs.jaicore.ml.core.evaluation.loss;

public class F1Measure extends FMeasure {

	private static final int BETA = 1;

	public F1Measure(final int positiveClass) {
		super(BETA, positiveClass);
	}

}
