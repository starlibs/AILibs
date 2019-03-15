package jaicore.ml.core.evaluation.measure.multilabel;

import jaicore.ml.core.evaluation.measure.LossScoreTransformer;

/**
 * Fitness function for a linear combination of 4 well-known multi-label metrics: ExactMatch, Hamming, Rank and F1MacroAverageL.
 *
 * @author mwever
 */
public class OverviewMeasure extends LossScoreTransformer<double[]> implements IMultilabelMeasure {

	private static final OverviewMeasureLoss OVERVIEW_MEASURE_LOSS = new OverviewMeasureLoss();

	public OverviewMeasure() {
		super(OVERVIEW_MEASURE_LOSS);
	}
}
