package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Arrays;

import meka.core.Metrics;

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
		double exactMatchLoss = EXACT_MATCH.calculateMeasure(actual, expected);
		double hammingLoss = HAMMING.calculateMeasure(actual, expected);
		double f1MacroLoss = F1_MACRO_AVG_L.calculateMeasure(actual, expected);
		double rankLoss = Metrics.L_RankLoss(Arrays.stream(expected).mapToInt(x -> (int) x).toArray(), actual);
		System.out.println(Arrays.toString(actual) + " " + Arrays.toString(expected));

		System.out.println("ExactMatch: " + exactMatchLoss + " hamming: " + hammingLoss + " f1: " + f1MacroLoss + " rank: " + rankLoss);

		return (exactMatchLoss + hammingLoss + f1MacroLoss + rankLoss) / 4.0;
	}

}