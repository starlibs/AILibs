package jaicore.ml.dyadranking.loss;

import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * A wrapper for dyad ranking loss that enables already implemented multi label
 * classification loss functions to be used in this context.
 * 
 * @author Helena Graf
 *
 */
public class DyadRankingMLLossFunctionWrapper implements DyadRankingLossFunction {

	/* the measure used internally to compute the loss */
	private ADecomposableDoubleMeasure<double[]> measure;

	/**
	 * Constructs a new loss function wrapper that uses the given measure to compute
	 * the loss between a correct and predicted dyad ranking.
	 * 
	 * @param measure
	 *            the measure to use
	 */
	public DyadRankingMLLossFunctionWrapper(ADecomposableDoubleMeasure<double[]> measure) {
		this.measure = measure;
	}

	@Override
	public double loss(IDyadRankingInstance actual, IDyadRankingInstance predicted) {

		// Convert Ranking to doubles
		double[] actualLabels = new double[actual.length()];
		double[] predictedLabels = new double[predicted.length()];

		for (int i = 0; i < actualLabels.length; i++) {
			actualLabels[i] = i;
			for (int j = 0; j < predictedLabels.length; j++) {
				if (predicted.getDyadAtPosition(i).getAlternative()
						.equals(actual.getDyadAtPosition(j).getAlternative())) {
					predictedLabels[j] = i;
					break;
				}
			}
		}

		// Compute loss
		return measure.calculateMeasure(actualLabels, predictedLabels);
	}

}
