package ai.libs.jaicore.ml.core.predictivemodel;

import org.api4.java.ai.ml.AILabeledAttributeArrayDataset;
import org.api4.java.ai.ml.IDataset;

import ai.libs.jaicore.ml.core.exception.TrainingException;

/**
 * The {@link IBatchLearner} models a learning algorithm which works in a batch
 * fashion, i.e. takes a whole {@link AILabeledAttributeArrayDataset} as training input. It can be
 * trained based on an {@link AILabeledAttributeArrayDataset} in order to make predictions.
 *
 * @author Alexander Hetzer, Julian Lienen
 *
 * @param <T>
 *            The type of the target that this {@link IBatchLearner} predicts.
 * @param <I>
 *            The type of the instances stored in the data set specified by the generic parameter <D>.
 * @param <D>
 *            The type of the data set used to learn from and predict batches.
 */
public interface IBatchLearner<T, I, D extends IDataset<I>> extends IPredictiveModel<T, I, D> {

	/**
	 * Trains this {@link IBatchLearner} using the given {@link AILabeledAttributeArrayDataset}.
	 *
	 * @param dataset
	 *            The {@link AILabeledAttributeArrayDataset} which should be used for the training.
	 * @throws TrainingException
	 *             If something fails during the training process.
	 */
	public void train(D dataset) throws TrainingException;
}
