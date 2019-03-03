package jaicore.ml.dyadranking.util;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import jaicore.ml.core.dataset.IInstance;
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
public class DyadNormalScaler  extends AbstractDyadScaler {

	/**
	 * Transforms only the instances of each dyad according to the mean and standard
	 * of the data the scaler has been fit to.
	 * 
	 * @param dataset
	 *            The dataset of which the instances are to be standardized.
	 */
	public void transformInstances(DyadRankingDataset dataset) {
		
		int lengthX = dataset.get(0).getDyadAtPosition(0).getInstance().length();
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			for (Dyad dyad : drInstance) {
				for (int i = 0; i < lengthX; i++) {
					double value = dyad.getInstance().getValue(i);
					if (value != 0.0d)
						value /= Math.sqrt(statsX[i].getSumsq());
					dyad.getInstance().setValue(i, value);
				}
			}
		}
	}

	/**
	 * Transforms only the alternatives of each dyad according to the mean and
	 * standard deviation of the data the scaler has been fit to.
	 * 
	 * @param dataset
	 *            The dataset of which the alternatives are to be standardized.
	 */
	public void transformAlternatives(DyadRankingDataset dataset) {
		int lengthY = dataset.get(0).getDyadAtPosition(0).getAlternative().length();
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			for (Dyad dyad : drInstance) {
				for (int i = 0; i < lengthY; i++) {
					double value = dyad.getAlternative().getValue(i);
					if (value != 0.0d)
						value /= Math.sqrt(statsY[i].getSumsq());
					dyad.getAlternative().setValue(i, value);
				}

			}
		}
	}
	
	@Override
	public void transformInstances(DyadRankingDataset dataset, List<Integer> ignoredIndices) {
		int lengthX = dataset.get(0).getDyadAtPosition(0).getInstance().length();
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			for (Dyad dyad : drInstance) {
				for (int i = 0; i < lengthX; i++) {
					if (!ignoredIndices.contains(i)) {
						double value = dyad.getInstance().getValue(i);
						if (value != 0.0d)
							value /= Math.sqrt(statsX[i].getSumsq());
						dyad.getInstance().setValue(i, value);
					}
				}
			}
		}
	}

	@Override
	public void transformAlternatives(DyadRankingDataset dataset, List<Integer> ignoredIndices) {
		int lengthY = dataset.get(0).getDyadAtPosition(0).getAlternative().length();
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			for (Dyad dyad : drInstance) {
				for (int i = 0; i < lengthY; i++) {
					if (!ignoredIndices.contains(i)) {
						double value = dyad.getAlternative().getValue(i);
						if (value != 0.0d)
							value /= Math.sqrt(statsY[i].getSumsq());
						dyad.getAlternative().setValue(i, value);
					}
				}
			}
		}
	}
}
