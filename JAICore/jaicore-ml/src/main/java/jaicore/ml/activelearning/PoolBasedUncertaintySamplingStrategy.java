package jaicore.ml.activelearning;

import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.predictivemodel.ICertaintyProvider;
import jaicore.ml.core.predictivemodel.IOnlineLearner;

/**
 * A simple pool-based uncertainty sampling strategy, which assesses certainty
 * for all instances in the pool and picks the instance with least certainty for
 * the next query.
 * 
 * @author Jonas Hanselle
 *
 * @param <TARGET>
 */
public class PoolBasedUncertaintySamplingStrategy<TARGET> implements ISelectiveSamplingStrategy {

	private ICertaintyProvider<TARGET> certaintyProvider;
	private IActiveLearningPoolProvider poolProvider;

	public PoolBasedUncertaintySamplingStrategy(ICertaintyProvider<TARGET> certaintyProivder,
			IOnlineLearner<TARGET> onlineLearner, IActiveLearningPoolProvider poolProvider) {
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
