package ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.featuretransform;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingDataset;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;

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
	public IVector transform(IDyad dyad);

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
	default Map<IDyadRankingInstance, Map<IDyad, IVector>> getPreComputedFeatureTransforms(final IDyadRankingDataset dataset) {
		Map<IDyadRankingInstance, Map<IDyad, IVector>> featureTransforms = new HashMap<>();
		for (IDyadRankingInstance instance : dataset) {
			IDyadRankingInstance rankingInstance = instance;
			Map<IDyad, IVector> transforms = new HashMap<>();
			for (int i = 0; i < rankingInstance.getLabel().size(); i++) {
				transforms.put(rankingInstance.getLabel().get(i), this.transform(rankingInstance.getLabel().get(i)));
			}
			featureTransforms.put(rankingInstance, transforms);
		}
		return featureTransforms;
	}
}
