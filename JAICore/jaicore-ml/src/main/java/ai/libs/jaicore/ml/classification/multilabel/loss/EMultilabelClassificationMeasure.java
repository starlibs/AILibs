package ai.libs.jaicore.ml.classification.multilabel.loss;

import org.api4.java.ai.ml.classification.multilabel.evaluation.loss.IMultiLabelClassificationMeasure;

public enum EMultilabelClassificationMeasure {
	EXACT_MATCH(new ExactMatch()), INSTANCE_F1(new InstanceWiseF1()), LABEL_F1(new F1MacroAverageL()), HAMMING(new Hamming()), JACCARD(new JaccardScore()), RANKLOSS(new RankLoss());

	private IMultiLabelClassificationMeasure measure;

	private EMultilabelClassificationMeasure(final IMultiLabelClassificationMeasure measure) {
		this.measure = measure;
	}

	public IMultiLabelClassificationMeasure getMeasure() {
		return this.measure;
	}
}
