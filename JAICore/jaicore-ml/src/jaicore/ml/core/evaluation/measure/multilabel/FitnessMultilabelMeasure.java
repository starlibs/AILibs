package jaicore.ml.core.evaluation.measure.multilabel;

import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;

/**
 * Measure combining exact match, hamming loss, f1macroavgL and rankloss. Here
 * implemented in inverse.
 * 
 * @author Helena Graf
 *
 */
public class FitnessMultilabelMeasure extends ADecomposableDoubleMeasure<double[]> {

	private ZeroOneLossMultilabelMeasure exactMatchLoss = new ZeroOneLossMultilabelMeasure();
	private HammingLossMultilabelEvaluator hammingLoss = new HammingLossMultilabelEvaluator();
	private InverseF1MacroAverageLMultilabelMeasure inverseMacroAvgL = new InverseF1MacroAverageLMultilabelMeasure();
	private RankLossMultilabelEvaluator rankLoss = new RankLossMultilabelEvaluator();

	@Override
	public Double calculateMeasure(double[] actual, double[] expected) {
		return (exactMatchLoss.calculateMeasure(actual, expected) + hammingLoss.calculateMeasure(actual, expected))
				+ inverseMacroAvgL.calculateMeasure(actual, expected)
				+ rankLoss.calculateMeasure(actual, expected) / 4.0;
	}

}
