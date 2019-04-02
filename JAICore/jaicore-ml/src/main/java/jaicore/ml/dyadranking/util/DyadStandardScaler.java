package jaicore.ml.dyadranking.util;

import java.util.List;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;

/**
 * A scaler that can be fit to a certain dataset and then be used to standardize
 * datasets, i.e. transform the data to have a mean of 0 and a standard
 * deviation of 1 according to the data it was fit to.
 * 
 * @author Michael Braun, Jonas Hanselle, Mirko Jürgens
 *
 */
public class DyadStandardScaler extends AbstractDyadScaler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void transformInstances(Dyad dyad, List<Integer> ignoredIndices) {
		for (int i = 0; i < dyad.getInstance().length(); i++) {
			if (!ignoredIndices.contains(i)) {
				double value = dyad.getInstance().getValue(i);
				value -= statsX[i].getMean();
				if (statsX[i].getStandardDeviation() != 0)
					value /= statsX[i].getStandardDeviation();
				dyad.getInstance().setValue(i, value);
			}
		}
	}

	@Override
	public void transformAlternatives(Dyad dyad, List<Integer> ignoredIndices) {
		for (int i = 0; i < dyad.getAlternative().length(); i++) {
			if (!ignoredIndices.contains(i)) {
				double value = dyad.getAlternative().getValue(i);
				value -= statsY[i].getMean();
				if (statsY[i].getStandardDeviation() != 0)
					value /= statsY[i].getStandardDeviation();
				dyad.getAlternative().setValue(i, value);
			}
		}
	}

	@Override
	public void transformInstaceVector(Vector vector, List<Integer> ignoredIndices) {
		for (int i = 0; i < vector.length(); i++) {
			if (!ignoredIndices.contains(i)) {
				double value = vector.getValue(i);
				value -= statsX[i].getMean();
				if (statsX[i].getStandardDeviation() != 0)
					value /= statsX[i].getStandardDeviation();
				vector.setValue(i, value);
			}
		}
	}
}
