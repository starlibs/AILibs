package ai.libs.jaicore.ml.ranking.dyad.learner.optimizing;

import java.util.Map;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingDataset;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;

import ai.libs.jaicore.math.gradientdescent.IGradientFunction;

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
	 * @param featureTransforms
	 *            the pre computed feature transformations
	 */
	void initialize(IDyadRankingDataset dataset, Map<IDyadRankingInstance, Map<IDyad, IVector>> featureTransforms);
}
