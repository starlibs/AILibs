package jaicore.ml.core.predictivemodel;

import jaicore.ml.core.dataset.AILabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.exception.TrainingException;

/**
 * The {@link IBatchLearner} models a learning algorithm which works in a batch
 * fashion, i.e. takes a whole {@link AILabeledAttributeArrayDataset} as training input. It can be
 * trained based on an {@link AILabeledAttributeArrayDataset} in order to make predictions.
 * 
 * @author Alexander Hetzer, Julian Lienen
 *
 * @param <TARGET>
 *            The type of the target that this {@link IBatchLearner} predicts.
 * @param <INSTANCE>
 *            The type of the instances stored in the data set specified by the
 *            generic parameter <DATASET>.
 * @param <DATASET>
 *            The type of the data set used to learn from and predict batches.
 */
public interface IBatchLearner<TARGET, INSTANCE, DATASET extends IDataset<INSTANCE>> extends IPredictiveModel<TARGET, INSTANCE, DATASET> {

	/**
	 * Trains this {@link IBatchLearner} using the given {@link AILabeledAttributeArrayDataset}.
	 * 
	 * @param dataset
	 *            The {@link AILabeledAttributeArrayDataset} which should be used for the training.
	 * @throws TrainingException
	 *             If something fails during the training process.
	 */
	public void train(DATASET dataset) throws TrainingException;
}
