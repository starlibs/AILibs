package ai.libs.jaicore.ml.core.predictivemodel;

import org.api4.java.ai.ml.algorithm.predictivemodel.IPredictiveModel;
import org.api4.java.ai.ml.core.dataset.IDataset;

/**
 * Abstract extension of {@link IPredictiveModel} to be able to construct
 * prediction of the given <T> type.
 *
 * @author Julian Lienen
 *
 * @param <T>
 *            The attribute type of the target that this {@link APredictiveModel} predicts.
 * @param <V>
 *            The value type of the target that this {@link APredictiveModel} predicts.
 * @param <I>
 *            The type of the instances stored in the data set specified by the generic parameter <D>.
 * @param <D>
 *            The type of the data set used to learn from and predict batches.
 */
public abstract class APredictiveModel<T, V, I, D extends IDataset<I>>
implements IPredictiveModel<V, I, D> {

	/**
	 * Target type of the predicted values induced by the predicted model.
	 */
	private T targetType;

	/**
	 * Getter method for the given <code>targetType</code>.
	 *
	 * @return Returns the target type used for generating predictions
	 */
	public T getTargetType() {
		return this.targetType;
	}

	/**
	 * Setter method for the given <code>targetType</code>.
	 *
	 * @param targetType
	 *            The target type used for generating predictions
	 */
	public void setTargetType(final T targetType) {
		this.targetType = targetType;
	}
}
