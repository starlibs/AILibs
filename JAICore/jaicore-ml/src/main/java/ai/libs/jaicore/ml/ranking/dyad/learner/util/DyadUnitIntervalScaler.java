package ai.libs.jaicore.ml.ranking.dyad.learner.util;

import java.util.List;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingDataset;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;

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
	public void fit(final IDyadRankingDataset dataset) {
		super.fit(dataset);
		int lengthX = dataset.get(0).getLabel().get(0).getContext().length();
		this.lengthOfX = new double[lengthX];
		for (int i = 0; i < lengthX; i++) {
			this.lengthOfX[i] = Math.sqrt(this.statsX[i].getSumsq());
		}
		int lengthY = dataset.get(0).getLabel().get(0).getAlternative().length();
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
	public void transformInstances(final IDyadRankingDataset dataset, final List<Integer> ignoredIndices) {
		int lengthX = dataset.get(0).getLabel().get(0).getContext().length();
		for (IDyadRankingInstance instance : dataset) {
			for (IDyad dyad : instance) {
				for (int i = 0; i < lengthX; i++) {
					double value = dyad.getContext().getValue(i);
					if (value != 0.0d) {
						value /= this.lengthOfX[i];
					}
					dyad.getContext().setValue(i, value);
				}
			}
		}
	}

	@Override
	public void transformAlternatives(final IDyadRankingDataset dataset, final List<Integer> ignoredIndices) {
		int lengthY = dataset.get(0).getLabel().get(0).getAlternative().length();
		for (IDyadRankingInstance instance : dataset) {
			for (IDyad dyad : instance) {
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
	public void transformInstaceVector(final IVector vector, final List<Integer> ignoredIndices) {
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
	public void transformInstances(final IDyad dyad, final List<Integer> ignoredIndices) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public void transformAlternatives(final IDyad dyad, final List<Integer> ignoredIndices) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

}
