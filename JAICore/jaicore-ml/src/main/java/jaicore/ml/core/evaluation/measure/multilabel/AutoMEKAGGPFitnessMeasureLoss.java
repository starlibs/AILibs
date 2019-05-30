package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Arrays;
import java.util.List;

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
		return (exactMatchLoss + hammingLoss + f1MacroLoss + rankLoss) / 4.0;
	}

	@Override
	public Double calculateAvgMeasure(final List<double[]> actual, final List<double[]> expected) {
		double[][] ypred = new double[actual.size()][];
		int[][] ypredint = new int[actual.size()][];
		for (int i = 0; i < actual.size(); i++) {
			ypred[i] = actual.get(i);
			ypredint[i] = Arrays.stream(actual.get(i)).mapToInt(x -> (x >= 0.5) ? 1 : 0).toArray();
		}

		int[][] y = new int[expected.size()][];
		for (int i = 0; i < expected.size(); i++) {
			y[i] = Arrays.stream(expected.get(i)).mapToInt(x -> (x >= 0.5) ? 1 : 0).toArray();
		}

		double hamming = Metrics.L_Hamming(y, ypredint);
		double rank = Metrics.L_RankLoss(y, ypred);
		double macroF = Metrics.P_FmacroAvgL(y, ypredint);
		double exactMatch = Metrics.P_Accuracy(y, ypredint);
		double fitnessMeasure = (((1 - hamming) + (1 - rank) + macroF + exactMatch) / 4);
		return 1 - fitnessMeasure;
	}

}