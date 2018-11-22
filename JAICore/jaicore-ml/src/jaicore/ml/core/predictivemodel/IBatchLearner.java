package jaicore.ml.core.predictivemodel;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.exception.TrainingException;

/**
 * The {@link IBatchLearner} models a learning algorithm which works in a batch fashion, i.e. takes a whole {@link IDataset} as training input. It can be trained based on an {@link IDataset} in order
 * to make predictions.
 * 
 * @author Alexander Hetzer
 *
 * @param <TARGET>
 *            The type of the target that this {@link IBatchLearner} predicts.
 */
public interface IBatchLearner<TARGET> extends IPredictiveModel<TARGET> {

	/**
	 * Trains this {@link IBatchLearner} using the given {@link IDataset}.
	 * 
	 * @param dataset
	 *            The {@link IDataset} which should be used for the training.
	 * @throws TrainingException
	 *             If something fails during the training process.
	 */
	public void train(IDataset dataset) throws TrainingException;
}
