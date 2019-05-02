package jaicore.ml.core.evaluation.measure.singlelabel;

public class MeanSquaredErrorLoss extends ASquaredErrorLoss {

	/**
	 * Automatically generated version UID for serialization.
	 */
	private static final long serialVersionUID = 8167100759563769537L;
	
	@Override
	public Double calculateMeasure(final Double actual, final Double expected) {
		return Math.pow(actual-expected, 2);
	}
}
