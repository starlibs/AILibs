package jaicore.ml.core.predictivemodel;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.attribute.IAttributeType;

/**
 * Abstract extension of {@link IPredictiveModel} to be able to construct
 * prediction of the given <TARGET> type.
 * 
 * @author Julian Lienen
 *
 * @param <TARGET>
 *            The type of the target that this {@link APredictiveModel}
 *            predicts.
 * @param <INSTANCE>
 *            The type of the instances stored in the data set specified by the
 *            generic parameter <DATASET>.
 * @param <DATASET>
 *            The type of the data set used to learn from and predict batches.
 */
public abstract class APredictiveModel<TARGET, INSTANCE extends IInstance, DATASET extends IDataset<INSTANCE>>
		implements IPredictiveModel<TARGET, INSTANCE, DATASET> {

	/**
	 * Target type of the predicted values induced by the predicted model.
	 */
	private IAttributeType<?> targetType;

	/**
	 * Getter method for the given <code>targetType</code>.
	 * 
	 * @return Returns the target type used for generating predictions
	 */
	public IAttributeType<?> getTargetType() {
		return targetType;
	}

	/**
	 * Setter method for the given <code>targetType</code>.
	 * 
	 * @param targetType
	 *            The target type used for generating predictions
	 */
	public void setTargetType(IAttributeType<?> targetType) {
		this.targetType = targetType;
	}
}
