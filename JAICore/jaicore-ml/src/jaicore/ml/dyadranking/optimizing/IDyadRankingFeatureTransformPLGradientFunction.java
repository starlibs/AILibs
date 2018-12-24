package jaicore.ml.dyadranking.optimizing;

import jaicore.ml.core.optimizing.IGradientFunction;
import jaicore.ml.dyadranking.algorithm.IDyadFeatureTransform;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;

/**
 * Represents a differentiable function in the context of dyad ranking based on
 * feature transformation Placket-Luce models.
 * 
 * @author Helena Graf
 *
 */
public interface IDyadRankingFeatureTransformPLGradientFunction extends IGradientFunction {

	/**
	 * Initialize the function with the given data set and feature transformation
	 * method.
	 * 
	 * @param dataset
	 *            the dataset to use
	 * @param featureTransform
	 *            the feature transformation method to use
	 */
	void initialize(DyadRankingDataset dataset, IDyadFeatureTransform featureTransform);
}
