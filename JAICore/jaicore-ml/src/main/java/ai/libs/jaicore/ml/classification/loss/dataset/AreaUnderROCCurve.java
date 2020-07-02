package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

public class AreaUnderROCCurve extends ASingleLabelPredictionPerformanceMeasure {

	private final Object positiveClass;

	public AreaUnderROCCurve(final Object positiveClass) {
		this.positiveClass = positiveClass;
	}

	public Object getPositiveClass() {
		return this.positiveClass;
	}

	@Override
	public double score(final List<? extends Integer> expected, final List<? extends ISingleLabelClassification> predicted) {
		throw new UnsupportedOperationException("AUROC cannot be implemented as we do not have any class probabilities available");
	}

}
