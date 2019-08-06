package ai.libs.jaicore.ml.activelearning;

import org.api4.java.ai.ml.activelearning.IActiveLearningPoolProvider;
import org.api4.java.ai.ml.activelearning.ISelectiveSamplingStrategy;
import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;
import org.api4.java.ai.ml.learner.IProbabilisticPredictor;
import org.api4.java.ai.ml.learner.predict.PredictionException;

/**
 * A simple pool-based uncertainty sampling strategy, which assesses certainty
 * for all instances in the pool and picks the instance with least certainty for
 * the next query.
 *
 * @author Jonas Hanselle
 *
 * @param <Y>
 *            TARGET
 * @param <I>
 *            The class of instances
 * @param <D>
 *            The class of the dataset
 */
public class PoolBasedUncertaintySamplingStrategy<X, Y, I extends IFeatureInstance<X> & ILabeledInstance<Y>, D extends ISupervisedDataset<X, Y, I>> implements ISelectiveSamplingStrategy<I> {

	private IProbabilisticPredictor<X, Y, I, D> certaintyProvider;
	private IActiveLearningPoolProvider<I> poolProvider;

	public PoolBasedUncertaintySamplingStrategy(final IProbabilisticPredictor<X, Y, I, D> certaintyProivder, final IActiveLearningPoolProvider<I> poolProvider) {
		this.certaintyProvider = certaintyProivder;
		this.poolProvider = poolProvider;
	}

	@Override
	public I nextQueryInstance() throws PredictionException, InterruptedException {
		double currentlyLowestCertainty = Double.MAX_VALUE;
		I currentlyLeastCertainInstance = null;
		for (I instance : this.poolProvider.getPool()) {
			double currentCertainty = this.certaintyProvider.getCertainty(instance);
			if (currentCertainty < currentlyLowestCertainty) {
				currentlyLowestCertainty = currentCertainty;
				currentlyLeastCertainInstance = instance;
			}
		}
		return currentlyLeastCertainInstance;
	}
}
