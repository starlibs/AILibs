package jaicore.ml.dyadranking.util;

import java.util.List;

import org.nd4j.nativeblas.Nd4jCpu.IGenerator;

import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * A scaler that can be fit to a certain dataset and then be used to standardize
 * datasets, i.e. transform the data to have a mean of 0 and a standard
 * deviation of 1 according to the data it was fit to.
 * 
 * @author Michael Braun, Jonas Hanselle
 *
 */
public class DyadStandardScaler extends AbstractDyadScaler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Transforms only the instances of each dyad according to the mean and standard
	 * of the data the scaler has been fit to.
	 * 
	 * @param dataset The dataset of which the instances are to be standardized.
	 */
	public void transformInstances(DyadRankingDataset dataset) {
		int lengthX = dataset.get(0).getDyadAtPosition(0).getInstance().length();
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			for (Dyad dyad : drInstance) {
				for (int i = 0; i < lengthX; i++) {
					double value = dyad.getInstance().getValue(i);
					value -= statsX[i].getMean();
					value /= statsX[i].getStandardDeviation();
					dyad.getInstance().setValue(i, value);
				}
			}
		}
	}

	/**
	 * Transforms only the alternatives of each dyad according to the mean and
	 * standard deviation of the data the scaler has been fit to.
	 * 
	 * @param dataset The dataset of which the alternatives are to be standardized.
	 */
	public void transformAlternatives(DyadRankingDataset dataset) {
		int lengthY = dataset.get(0).getDyadAtPosition(0).getAlternative().length();
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			for (Dyad dyad : drInstance) {
				for (int i = 0; i < lengthY; i++) {
					double value = dyad.getAlternative().getValue(i);
					value -= statsY[i].getMean();
					value /= statsY[i].getStandardDeviation();
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
						value -= statsX[i].getMean();
						value /= statsX[i].getStandardDeviation();
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
						value -= statsY[i].getMean();
						value /= statsY[i].getStandardDeviation();
						dyad.getAlternative().setValue(i, value);
					}
				}
			}
		}
	}
}
