package jaicore.ml.core.evaluation.measure.multilabel;

import jaicore.ml.core.evaluation.measure.LossScoreTransformer;

/**
* Compute the inverted F1 measure macro averaged by label.
*
* @author helegraf, mwever
*
*/
public class F1MacroAverageLLoss extends LossScoreTransformer<double[]> implements IMultilabelMeasure {

	private static final F1MacroAverageL F1_MACRO_AVG_L = new F1MacroAverageL();

	public F1MacroAverageLLoss() {
		super(F1_MACRO_AVG_L);
	}

}