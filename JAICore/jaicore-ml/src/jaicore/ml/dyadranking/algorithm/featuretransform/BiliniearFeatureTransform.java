package jaicore.ml.dyadranking.algorithm.featuretransform;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;

/**
 * Implementation of the feature transformation method using the Kroenecker
 * Product.
 * 
 * @author Helena Graf, Mirko Jürgens
 *
 */
public class BiliniearFeatureTransform implements IDyadFeatureTransform {

	@Override
	public Vector transform(Dyad dyad) {
		Vector x = dyad.getInstance();
		Vector y = dyad.getAlternative();
		return x.kroneckerProduct(y.asArray());
	}

	@Override
	public int getTransformedVectorLength(int alternativeLength, int instanceLength) {
		return alternativeLength * instanceLength;
	}

}
