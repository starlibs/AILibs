package jaicore.ml.core.evaluation.measure.multilabel;

/**
 * Measure combining exact match, hamming loss, f1macroavgL and rankloss. Here
 * implemented in inverse.
 *
 * de Sá, Alex GC, Gisele L. Pappa, and Alex A. Freitas. "Towards a method for automatically selecting and configuring multi-label
 * classification algorithms." Proceedings of the Genetic and Evolutionary Computation Conference Companion. ACM, 2017.
 *
 * @author helegraf, mwever
 *
 */
public class AutoMEKAGGPFitnessMeasureLoss extends ADecomposableMultilabelMeasure {

	private static final ExactMatchLoss EXACT_MATCH = new ExactMatchLoss();
	private static final HammingLoss HAMMING = new HammingLoss();
	private static final F1MacroAverageLLoss F1_MACRO_AVG_L = new F1MacroAverageLLoss();
	private static final RankLoss RANK = new RankLoss();

	@Override
	public Double calculateMeasure(final double[] actual, final double[] expected) {
		return (EXACT_MATCH.calculateMeasure(actual, expected) + HAMMING.calculateMeasure(actual, expected) + F1_MACRO_AVG_L.calculateMeasure(actual, expected) + RANK.calculateMeasure(actual, expected)) / 4.0;
	}

}