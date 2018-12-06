package jaicore.ml.dyadranking.optimizing;

import java.util.HashMap;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.IDyadFeatureTransform;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;

/**
 * Implements the negative log-likelihood function for the feature
 * transformation Placket-Luce dyad ranker.
 * 
 * @author Helena Graf
 *
 */
public class DyadRankingFeatureTransformNegativeLogLikelihood
		implements IDyadRankingFeatureTransformPLGradientDescendableFunction {

	/* the dataset used by this function */
	private DyadRankingDataset dataset;

	/* the feature transformation method used by this function */
	private IDyadFeatureTransform featureTransform;

	@Override
	public void initialize(DyadRankingDataset dataset, IDyadFeatureTransform featureTransform) {
		this.dataset = dataset;
		this.featureTransform = featureTransform;
	}

	@Override
	public double apply(Vector vector) {
		double result = 0;

		HashMap<Dyad, Vector> featureTransforms = new HashMap<>();
		for (IInstance instance : dataset) {
			for (Dyad dyad : ((DyadRankingInstance) instance)) {
				result -= vector.dotProduct(getOrCreateFeatureTransform(dyad, featureTransforms));

				double intermediateResult = 0;

				for (Dyad innerDyad : ((DyadRankingInstance) instance)) {
					intermediateResult += Math
							.exp(vector.dotProduct(getOrCreateFeatureTransform(innerDyad, featureTransforms)));
				}

				result += Math.log(intermediateResult);
			}
		}

		return result;
	}

	private Vector getOrCreateFeatureTransform(Dyad dyad, HashMap<Dyad, Vector> featureTransforms) {
		if (featureTransforms.containsKey(dyad)) {
			return featureTransforms.get(dyad);
		} else {
			Vector newFeatureTransform = featureTransform.transform(dyad);
			featureTransforms.put(dyad, newFeatureTransform);
			return newFeatureTransform;
		}
	}

}
