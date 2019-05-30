package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Arrays;

import meka.core.Metrics;

public class AutoMekaGGPFitness {

	public double calculateMeasure(final double[][] ypred, final int[][] y) {
		int[][] ypredInt = new int[ypred.length][];
		for (int i = 0; i < ypred.length; i++) {
			ypredInt[i] = Arrays.stream(ypred[i]).mapToInt(x -> (x >= 0.5) ? 1 : 0).toArray();
		}

		double hamming = Metrics.P_Hamming(y, ypredInt);
		double rank = Metrics.L_RankLoss(y, ypred);
		double f1macroL = Metrics.P_FmacroAvgL(y, ypredInt);
		double exactMatch = Metrics.P_Accuracy(y, ypredInt);

		return (hamming + (1 - rank) + f1macroL + exactMatch) / 4;
	}
}
