package jaicore.ml.dyadranking.util;

import java.util.List;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * A scaler that can be fit to a certain dataset and then be used to normalize
 * datasets, i.e. transform the data to have a length of 1.
 *
 * @author Mirko Jürgens
 *
 */
public class DyadUnitIntervalScaler extends AbstractDyadScaler {

	private double[] lengthOfX;

	private double[] lengthOfY;

	@Override
	public void fit(final DyadRankingDataset dataset) {
		super.fit(dataset);
		int lengthX = dataset.get(0).getDyadAtPosition(0).getInstance().length();
		this.lengthOfX = new double[lengthX];
		for (int i = 0; i < lengthX; i++) {
			this.lengthOfX[i] = Math.sqrt(this.statsX[i].getSumsq());
		}
		int lengthY = dataset.get(0).getDyadAtPosition(0).getAlternative().length();
		this.lengthOfY = new double[lengthY];
		for (int i = 0; i < lengthY; i++) {
			this.lengthOfY[i] = Math.sqrt(this.statsY[i].getSumsq());
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -6732663643697649308L;

	@Override
	public void transformInstances(final DyadRankingDataset dataset, final List<Integer> ignoredIndices) {
		int lengthX = dataset.get(0).getDyadAtPosition(0).getInstance().length();
		for (IDyadRankingInstance instance : dataset) {
			for (Dyad dyad : instance) {
				for (int i = 0; i < lengthX; i++) {
					double value = dyad.getInstance().getValue(i);
					if (value != 0.0d) {
						value /= this.lengthOfX[i];
					}
					dyad.getInstance().setValue(i, value);
				}
			}
		}
	}

	@Override
	public void transformAlternatives(final DyadRankingDataset dataset, final List<Integer> ignoredIndices) {
		int lengthY = dataset.get(0).getDyadAtPosition(0).getAlternative().length();
		for (IDyadRankingInstance instance : dataset) {
			for (Dyad dyad : instance) {
				for (int i = 0; i < lengthY; i++) {
					double value = dyad.getAlternative().getValue(i);
					if (value != 0.0d) {
						value /= this.lengthOfY[i];
					}
					dyad.getAlternative().setValue(i, value);
				}
			}
		}
	}

	@Override
	public void transformInstaceVector(final Vector vector, final List<Integer> ignoredIndices) {
		for (int i = 0; i < vector.length(); i++) {
			if (!ignoredIndices.contains(i)) {
				double value = vector.getValue(i);
				if (value != 0.0d) {
					value /= Math.sqrt(this.statsX[i].getSumsq());
				}
				vector.setValue(i, value);
			}
		}
	}

	@Override
	public void transformInstances(final Dyad dyad, final List<Integer> ignoredIndices) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public void transformAlternatives(final Dyad dyad, final List<Integer> ignoredIndices) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

}
