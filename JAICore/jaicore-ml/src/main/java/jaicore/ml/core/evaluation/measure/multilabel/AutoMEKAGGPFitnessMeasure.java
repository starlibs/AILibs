package jaicore.ml.core.evaluation.measure.multilabel;

import jaicore.ml.core.evaluation.measure.LossScoreTransformer;

/**
 * Fitness function for a linear combination of 4 well-known multi-label metrics: ExactMatch, Hamming, Rank and F1MacroAverageL.
 *
 * de Sá, Alex GC, Gisele L. Pappa, and Alex A. Freitas. "Towards a method for automatically selecting and configuring multi-label
 * classification algorithms." Proceedings of the Genetic and Evolutionary Computation Conference Companion. ACM, 2017.
 *
 * @author mwever
 */
public class AutoMEKAGGPFitnessMeasure extends LossScoreTransformer<double[]> implements IMultilabelMeasure {

	private static final AutoMEKAGGPFitnessMeasureLoss AUTOMEKA_GGP_FITNESS_LOSS = new AutoMEKAGGPFitnessMeasureLoss();

	public AutoMEKAGGPFitnessMeasure() {
		super(AUTOMEKA_GGP_FITNESS_LOSS);
	}
}
