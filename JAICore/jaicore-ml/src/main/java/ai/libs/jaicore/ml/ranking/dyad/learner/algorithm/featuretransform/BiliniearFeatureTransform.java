package ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.featuretransform;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.common.math.IVector;

/**
 * Implementation of the feature transformation method using the Kroenecker
 * Product.
 *
 * @author Helena Graf, Mirko Jürgens
 *
 */
public class BiliniearFeatureTransform implements IDyadFeatureTransform {

	@Override
	public IVector transform(final IDyad dyad) {
		IVector x = dyad.getContext();
		IVector y = dyad.getAlternative();
		return x.kroneckerProduct(y.asArray());
	}

	@Override
	public int getTransformedVectorLength(final int alternativeLength, final int instanceLength) {
		return alternativeLength * instanceLength;
	}

}
