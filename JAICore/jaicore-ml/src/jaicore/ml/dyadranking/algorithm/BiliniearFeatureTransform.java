package jaicore.ml.dyadranking.algorithm;

import de.upb.isys.linearalgebra.DenseDoubleVector;
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

		Vector result = new DenseDoubleVector(dyad.getInstance().length() * dyad.getAlternative().length());

		for (int i = 0; i < dyad.getInstance().length(); i++) {
			for (int j = 0; j < dyad.getAlternative().length(); j++) {
				result.setValue(i * j + j, dyad.getInstance().getValue(i) * dyad.getAlternative().getValue(j));
			}
		}

		return result;
	}

}
