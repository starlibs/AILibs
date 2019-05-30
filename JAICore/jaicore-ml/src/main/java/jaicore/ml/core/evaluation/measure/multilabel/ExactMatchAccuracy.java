package jaicore.ml.core.evaluation.measure.multilabel;

import jaicore.ml.core.evaluation.measure.LossScoreTransformer;

/**
 * Computes the exact match of the predicted multi label vector and the expected.
 *
 * @author mwever
 */
public class ExactMatchAccuracy extends LossScoreTransformer<double[]> implements IMultilabelMeasure {

	private static final ExactMatchLoss LOSS = new ExactMatchLoss();

	public ExactMatchAccuracy() {
		super(LOSS);
	}

}
