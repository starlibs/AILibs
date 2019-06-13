package ai.libs.jaicore.ml.dyadranking.algorithm.featuretransform;

import ai.libs.jaicore.math.linearalgebra.Vector;
import ai.libs.jaicore.ml.dyadranking.Dyad;

/**
 * Implementation of the feature transformation method using the Kroenecker
 * Product.
 *
 * @author Helena Graf, Mirko Jürgens
 *
 */
public class BiliniearFeatureTransform implements IDyadFeatureTransform {

	@Override
	public Vector transform(final Dyad dyad) {
		Vector x = dyad.getInstance();
		Vector y = dyad.getAlternative();
		return x.kroneckerProduct(y.asArray());
	}

	@Override
	public int getTransformedVectorLength(final int alternativeLength, final int instanceLength) {
		return alternativeLength * instanceLength;
	}

}
