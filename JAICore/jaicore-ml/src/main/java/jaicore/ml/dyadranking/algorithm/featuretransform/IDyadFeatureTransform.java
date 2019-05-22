package jaicore.ml.dyadranking.algorithm.featuretransform;

import java.util.HashMap;
import java.util.Map;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Feature transformation interface for the
 * {@link FeatureTransformPLDyadRanker}.
 * 
 * @author Helena Graf, Mirko Jürgens
 *
 */
public interface IDyadFeatureTransform {

	/**
	 * Transform the instance of the given dyad (models the skill).
	 * 
	 * @param dyad
	 *            the dyad to transform
	 * @return the transformed instance values for the dyad
	 */
	public Vector transform(Dyad dyad);

	/**
	 * Get the length of the vector returned by the transform method.
	 * 
	 * @param alternativeLength
	 *            the length of the alternative vector of the transformed dyad
	 * @param instanceLength
	 *            the length of the instance vector of the transformed dyad
	 * @return the length of the transformed feature vector
	 */
	public int getTransformedVectorLength(int alternativeLength, int instanceLength);

	/**
	 * Precomputed the feature transforms for the dataset, this can speed up the
	 * runtime as the feature transform will be reduced to O(1) at the cost of O(n).
	 * 
	 * @param dataset
	 * @return the feature transform
	 */
	default Map<IDyadRankingInstance, Map<Dyad, Vector>> getPreComputedFeatureTransforms(DyadRankingDataset dataset) {
		Map<IDyadRankingInstance, Map<Dyad, Vector>> featureTransforms = new HashMap<>();
		for (IDyadRankingInstance instance : dataset) {
			IDyadRankingInstance rankingInstance = instance;
			Map<Dyad, Vector> transforms = new HashMap<>();
			for (int i = 0; i < rankingInstance.length(); i++) {
				transforms.put(rankingInstance.getDyadAtPosition(i),
						this.transform(rankingInstance.getDyadAtPosition(i)));
			}
			featureTransforms.put(rankingInstance, transforms);
		}
		return featureTransforms;
	}
}
