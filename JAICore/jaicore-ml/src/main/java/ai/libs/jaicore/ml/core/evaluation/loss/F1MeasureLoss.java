package ai.libs.jaicore.ml.core.evaluation.loss;

public class F1MeasureLoss extends FMeasure {

	private static final int BETA = 1;

	public F1MeasureLoss(final int positiveClass) {
		super(BETA, positiveClass);
	}

}
