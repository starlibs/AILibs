package jaicore.ml.dyadranking.algorithm;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;

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

}
