package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicHomogeneousPredictionPerformanceMeasure;

public class ErrorRate extends APredictionPerformanceMeasure<Object, Object> implements IDeterministicHomogeneousPredictionPerformanceMeasure<Object> {

	ErrorRate() {
		/* empty constructor to avoid direct instantiation. Use the enum instead. */
	}

	@Override
	public double loss(final List<?> expected, final List<?> actual) {
		this.checkConsistency(expected, actual);
		double sumOfZOLoss = 0.0;
		for (int i = 0; i < expected.size(); i++) {
			sumOfZOLoss += expected.get(i).equals(actual.get(i)) ? 0.0 : 1.0;
		}
		return sumOfZOLoss / expected.size();
	}

}
