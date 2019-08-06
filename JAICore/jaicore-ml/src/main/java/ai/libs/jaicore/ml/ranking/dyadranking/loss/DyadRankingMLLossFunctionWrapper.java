package ai.libs.jaicore.ml.ranking.dyadranking.loss;

import org.api4.java.ai.ml.IRanking;

import ai.libs.jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;
import ai.libs.jaicore.ml.ranking.dyadranking.Dyad;

/**
 * A wrapper for dyad ranking loss that enables already implemented multi label
 * classification loss functions to be used in this context.
 *
 * @author Helena Graf
 *
 */
public class DyadRankingMLLossFunctionWrapper implements IDyadRankingLossFunction {

	/* the measure used internally to compute the loss */
	private ADecomposableDoubleMeasure<double[]> measure;

	/**
	 * Constructs a new loss function wrapper that uses the given measure to compute
	 * the loss between a correct and predicted dyad ranking.
	 *
	 * @param measure
	 *            the measure to use
	 */
	public DyadRankingMLLossFunctionWrapper(final ADecomposableDoubleMeasure<double[]> measure) {
		this.measure = measure;
	}

	@Override
	public double loss(final IRanking<Dyad> expected, final IRanking<Dyad> actual) {

		// Convert Ranking to doubles
		double[] actualLabels = new double[expected.size()];
		double[] predictedLabels = new double[actual.size()];

		for (int i = 0; i < actualLabels.length; i++) {
			actualLabels[i] = i;
			for (int j = 0; j < predictedLabels.length; j++) {
				if (actual.get(i).getAlternative().equals(expected.get(j).getAlternative())) {
					predictedLabels[j] = i;
					break;
				}
			}
		}

		// Compute loss
		return this.measure.calculateMeasure(actualLabels, predictedLabels);
	}

}
