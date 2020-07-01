package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

public class AreaUnderROCCurve extends AHomogeneousPredictionPerformanceMeasure<Object> {

	private final Object positiveClass;

	public AreaUnderROCCurve(final Object positiveClass) {
		this.positiveClass = positiveClass;
	}

	public Object getPositiveClass() {
		return this.positiveClass;
	}

	@Override
	public double score(final List<?> expected, final List<?> actual) {
		throw new UnsupportedOperationException("AUROC cannot be implemented as we do not have any class probabilities available");
	}

}
