package jaicore.ml.core.evaluation.measure.multilabel;

import jaicore.ml.core.evaluation.measure.LossScoreTransformer;

/**
 * Measure for computing how similar two double vectors are according to hamming distance.
 *
 * @author mwever
 */
public class HammingAccuracy extends LossScoreTransformer<double[]> implements IMultilabelMeasure {

	/* Wrapped measure. */
	private static final HammingLoss HAMMING_LOSS = new HammingLoss();

	/**
	 * Standard c'tor.
	 */
	public HammingAccuracy() {
		super(HAMMING_LOSS);
	}

}
