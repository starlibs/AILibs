package jaicore.ml.activelearning;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.ILabeledInstance;
import jaicore.ml.core.predictivemodel.ICertaintyProvider;

/**
 * A simple pool-based uncertainty sampling strategy, which assesses certainty
 * for all instances in the pool and picks the instance with least certainty for
 * the next query.
 * 
 * @author Jonas Hanselle
 *
 * @param <T>
 *            TARGET
 * @param <I>
 *            The class of instances
 * @param <D>
 *            The class of the dataset
 */
public class PoolBasedUncertaintySamplingStrategy<T, I extends ILabeledInstance, D extends IDataset<I>> implements ISelectiveSamplingStrategy<I> {

	private ICertaintyProvider<T, I, D> certaintyProvider;
	private IActiveLearningPoolProvider<I> poolProvider;

	public PoolBasedUncertaintySamplingStrategy(ICertaintyProvider<T, I, D> certaintyProivder, IActiveLearningPoolProvider<I> poolProvider) {
		this.certaintyProvider = certaintyProivder;
		this.poolProvider = poolProvider;
	}

	@Override
	public I nextQueryInstance() {
		double currentlyLowestCertainty = Double.MAX_VALUE;
		I currentlyLeastCertainInstance = null;
		for (I instance : poolProvider.getPool()) {
			double currentCertainty = certaintyProvider.getCertainty(instance);
			if (currentCertainty < currentlyLowestCertainty) {
				currentlyLowestCertainty = currentCertainty;
				currentlyLeastCertainInstance = instance;
			}
		}
		return currentlyLeastCertainInstance;
	}
}
