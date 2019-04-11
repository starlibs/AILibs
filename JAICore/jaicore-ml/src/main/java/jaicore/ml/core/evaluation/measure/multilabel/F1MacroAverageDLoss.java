package jaicore.ml.core.evaluation.measure.multilabel;

import jaicore.ml.core.evaluation.measure.LossScoreTransformer;

/**
 * The F1 Macro Averaged by the number of instances measure.
 *
 * @author helegraf, mwever
 *
 */
public class F1MacroAverageDLoss extends LossScoreTransformer<double[]> implements IMultilabelMeasure {

	private static final F1MacroAverageD F1_MACRO_AVERAGE_D = new F1MacroAverageD();

	public F1MacroAverageDLoss() {
		super(F1_MACRO_AVERAGE_D);
	}

}