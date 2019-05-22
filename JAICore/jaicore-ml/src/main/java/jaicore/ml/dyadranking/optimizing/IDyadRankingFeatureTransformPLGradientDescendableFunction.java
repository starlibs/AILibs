package jaicore.ml.dyadranking.optimizing;

import java.util.Map;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.optimizing.IGradientDescendableFunction;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

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
	void initialize(DyadRankingDataset dataset, Map<IDyadRankingInstance, Map<Dyad, Vector>> featureTransform);
}
