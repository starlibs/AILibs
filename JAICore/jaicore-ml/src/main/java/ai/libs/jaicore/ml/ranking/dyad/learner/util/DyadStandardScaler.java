package ai.libs.jaicore.ml.ranking.dyad.learner.util;

import java.util.List;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;

import ai.libs.jaicore.math.linearalgebra.IVector;

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
	public void transformInstances(final IDyad dyad, final List<Integer> ignoredIndices) {
		for (int i = 0; i < ((IVector) dyad.getInstance()).length(); i++) {
			if (!ignoredIndices.contains(i)) {
				double value = ((IVector) dyad.getInstance()).getValue(i);
				value -= this.statsX[i].getMean();
				if (this.statsX[i].getStandardDeviation() != 0) {
					value /= this.statsX[i].getStandardDeviation();
				}
				((IVector) dyad.getInstance()).setValue(i, value);
			}
		}
	}

	@Override
	public void transformAlternatives(final IDyad dyad, final List<Integer> ignoredIndices) {
		for (int i = 0; i < ((IVector) dyad.getAlternative()).length(); i++) {
			if (!ignoredIndices.contains(i)) {
				double value = ((IVector) dyad.getAlternative()).getValue(i);
				value -= this.statsY[i].getMean();
				if (this.statsY[i].getStandardDeviation() != 0) {
					value /= this.statsY[i].getStandardDeviation();
				}
				((IVector) dyad.getAlternative()).setValue(i, value);
			}
		}
	}

	@Override
	public void transformInstaceVector(final IVector vector, final List<Integer> ignoredIndices) {
		for (int i = 0; i < vector.length(); i++) {
			if (!ignoredIndices.contains(i)) {
				double value = vector.getValue(i);
				value -= this.statsX[i].getMean();
				if (this.statsX[i].getStandardDeviation() != 0) {
					value /= this.statsX[i].getStandardDeviation();
				}
				vector.setValue(i, value);
			}
		}
	}
}
