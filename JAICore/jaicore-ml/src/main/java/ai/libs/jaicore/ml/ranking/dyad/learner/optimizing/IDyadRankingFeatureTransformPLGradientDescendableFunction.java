package ai.libs.jaicore.ml.ranking.dyad.learner.optimizing;

import java.util.Map;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingDataset;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;

import ai.libs.jaicore.math.gradientdescent.IGradientDescendableFunction;

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
	 *            the feature precomputed feature transforms
	 */
	void initialize(IDyadRankingDataset dataset, Map<IDyadRankingInstance, Map<IDyad, IVector>> featureTransform);
}
