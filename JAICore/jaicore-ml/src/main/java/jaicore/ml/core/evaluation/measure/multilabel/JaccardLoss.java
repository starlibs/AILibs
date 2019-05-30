package jaicore.ml.core.evaluation.measure.multilabel;

import jaicore.ml.core.evaluation.measure.LossScoreTransformer;

public class JaccardLoss extends LossScoreTransformer<double[]> implements IMultilabelMeasure {

	private static final JaccardScore JACCARD_SCORE = new JaccardScore();

	public JaccardLoss() {
		super(JACCARD_SCORE);
	}

}
