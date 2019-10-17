package ai.libs.jaicore.ml.classification.multilabel.loss;

import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.evaluation.loss.ILossFunction;

import meka.core.Metrics;

/**
 * Measure combining exact match, hamming loss, f1macroavgL and rankloss. Here
 * implemented in inverse.
 *
 * de S�, Alex GC, Gisele L. Pappa, and Alex A. Freitas. "Towards a method for automatically selecting and configuring multi-label
 * classification algorithms." Proceedings of the Genetic and Evolutionary Computation Conference Companion. ACM, 2017.
 *
 * @author helegraf, mwever
 *
 */
public class AutoMEKAGGPFitnessMeasureLoss implements ILossFunction<double[]> {

	@Override
	public double loss(final List<double[]> actual, final List<double[]> expected) {
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