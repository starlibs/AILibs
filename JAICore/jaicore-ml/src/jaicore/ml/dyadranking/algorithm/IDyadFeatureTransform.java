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

}
