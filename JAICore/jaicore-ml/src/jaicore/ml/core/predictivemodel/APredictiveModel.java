package jaicore.ml.core.predictivemodel;

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
 */
public abstract class APredictiveModel<TARGET> implements IPredictiveModel<TARGET> {

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
