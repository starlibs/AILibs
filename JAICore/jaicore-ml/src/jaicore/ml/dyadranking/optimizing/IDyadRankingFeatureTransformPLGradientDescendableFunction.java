package jaicore.ml.dyadranking.optimizing;

import jaicore.ml.dyadranking.algorithm.IDyadFeatureTransform;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;

/**
 * An interface for a differentiable function in the context of feature
 * transformation Placket-Luce dyad ranking.
 * 
 * @author Helena Graf
 *
 */
public interface IDyadRankingFeatureTransformPLGradientDescendableFunction extends IGradientDescendableFunction {

	/**
	 * Initializes the function with the given dataset.
	 * 
	 * @param dataset
	 *            the dataset to use
	 * @param featureTransform
	 *            the feature transformation method to use
	 */
	void initialize(DyadRankingDataset dataset, IDyadFeatureTransform featureTransform);
}
