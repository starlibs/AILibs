package ai.libs.jaicore.ml.classification.multilabel.loss;

import org.api4.java.ai.ml.core.evaluation.loss.ILossFunction;

public enum EMultilabelPerformanceMeasure {
	EXACT_MATCH(new ExactMatch());

	private ILossFunction loss;

	private EMultilabelPerformanceMeasure(final ILossFunction loss) {
		this.loss = loss;
	}

	public ILossFunction getLoss() {
		return this.loss;
	}
}
