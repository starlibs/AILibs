package jaicore.ml.core.evaluation.measure.multilabel;

import jaicore.ml.core.evaluation.measure.LossScoreTransformer;

/**
 * The F1 Macro Averaged by the number of instances measure.
 *
 * @author helegraf, mwever
 *
 */
public class InstanceWiseF1AsLoss extends LossScoreTransformer<double[]> implements IMultilabelMeasure {

	private static final InstanceWiseF1 F1_MACRO_AVERAGE_D = new InstanceWiseF1();

	public InstanceWiseF1AsLoss() {
		super(F1_MACRO_AVERAGE_D);
	}

}