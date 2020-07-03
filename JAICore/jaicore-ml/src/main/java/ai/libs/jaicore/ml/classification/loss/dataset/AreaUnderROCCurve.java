package ai.libs.jaicore.ml.classification.loss.dataset;

public class AreaUnderROCCurve extends AAreaUnderCurvePerformanceMeasure {

	public AreaUnderROCCurve(final int positiveClass) {
		super(positiveClass);
	}

	@Override
	public double getXValue(final int tp, final int fp, final int tn, final int fn) {
		// false positive rate
		return (double) fp / (fp + tn);
	}

	@Override
	public double getYValue(final int tp, final int fp, final int tn, final int fn) {
		// true positive rate
		return (double) tp / (tp + fn);
	}

}
