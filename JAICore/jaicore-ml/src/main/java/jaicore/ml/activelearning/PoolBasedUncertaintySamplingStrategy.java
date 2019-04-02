package jaicore.ml.activelearning;

import jaicore.ml.core.dataset.IInstance;
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
 */
public class PoolBasedUncertaintySamplingStrategy<T> implements ISelectiveSamplingStrategy {

	private ICertaintyProvider<T> certaintyProvider;
	private IActiveLearningPoolProvider poolProvider;

	public PoolBasedUncertaintySamplingStrategy(ICertaintyProvider<T> certaintyProivder,
			IActiveLearningPoolProvider poolProvider) {
		this.certaintyProvider = certaintyProivder;
		this.poolProvider = poolProvider;
	}

	@Override
	public IInstance nextQueryInstance() {
		double currentlyLowestCertainty = Double.MAX_VALUE;
		IInstance currentlyLeastCertainInstance = null;
		for (IInstance instance : poolProvider.getPool()) {
			double currentCertainty = certaintyProvider.getCertainty(instance);
			if (currentCertainty < currentlyLowestCertainty) {
				currentlyLowestCertainty = currentCertainty;
				currentlyLeastCertainInstance = instance;
			}
		}
		return currentlyLeastCertainInstance;
	}
}
