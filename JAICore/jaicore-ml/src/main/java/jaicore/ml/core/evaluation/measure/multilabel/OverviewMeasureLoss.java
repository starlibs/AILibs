package jaicore.ml.core.evaluation.measure.multilabel;

/**
* Measure combining exact match, hamming loss, f1macroavgL and rankloss. Here
* implemented in inverse.
*
* @author helegraf, mwever
*
*/
public class OverviewMeasureLoss extends ADecomposableMultilabelMeasure {

	private static final ExactMatchLoss EXACT_MATCH = new ExactMatchLoss();
	private static final HammingLoss HAMMING = new HammingLoss();
	private static final F1MacroAverageLLoss F1_MACRO_AVG_L = new F1MacroAverageLLoss();
	private static final RankLoss RANK = new RankLoss();

	@Override
	public Double calculateMeasure(final double[] actual, final double[] expected) {
		return (EXACT_MATCH.calculateMeasure(actual, expected) + HAMMING.calculateMeasure(actual, expected) + F1_MACRO_AVG_L.calculateMeasure(actual, expected) + RANK.calculateMeasure(actual, expected)) / 4.0;
	}

}