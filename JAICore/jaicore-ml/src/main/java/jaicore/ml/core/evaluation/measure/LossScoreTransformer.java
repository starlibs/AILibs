package jaicore.ml.core.evaluation.measure;

/**
 * This transformer transforms a decomposable double measure from a scoring function to a loss or vice versa.
 * It is required that the measured values lie in the interval [0,1]. Another requirement is that the input
 * domains of the two measures are the same.
 *
 * @author mwever
 *
 * @param <I> The input domain of the two measures.
 */
public class LossScoreTransformer<I> extends ADecomposableDoubleMeasure<I> {

	/* Measure to transform */
	private ADecomposableDoubleMeasure<I> measureToTransform;

	/**
	 * Constructor for setting the measure to be transformed from loss to score or vice versa.
	 * @param measure The measure to be transformed into the opposite.
	 */
	public LossScoreTransformer(final ADecomposableDoubleMeasure<I> measure) {
		this.measureToTransform = measure;
	}

	@Override
	public Double calculateMeasure(final I actual, final I expected) {
		return (1 - this.measureToTransform.calculateMeasure(actual, expected));
	}

}
