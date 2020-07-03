package ai.libs.jaicore.ml.classification.loss.dataset;

public class AreaUnderPrecisionRecallCurve extends AAreaUnderCurvePerformanceMeasure {

	public AreaUnderPrecisionRecallCurve(final int positiveClass) {
		super(positiveClass);
	}

	@Override
	public double getXValue(final int tp, final int fp, final int tn, final int fn) {
		// true positive rate / recall
		return (double) tp / (tp + fn);
	}

	@Override
	public double getYValue(final int tp, final int fp, final int tn, final int fn) {
		// precision
		if (tp + fp == 0) {
			return 1.0;
		}
		return (double) tp / (tp + fp);
	}

}
