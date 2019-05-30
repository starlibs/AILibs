package jaicore.ml.core.predictivemodel;

import jaicore.ml.core.dataset.IDataset;

/**
 * Abstract extension of {@link IPredictiveModel} to be able to construct
 * prediction of the given <TARGET> type.
 * 
 * @author Julian Lienen
 *
 * @param <TARGETTYPE>
 *            The attribute type of the target that this
 *            {@link APredictiveModel} predicts.
 * @param <TARGETVALUETYPE>
 *            The value type of the target that this {@link APredictiveModel}
 *            predicts.
 * @param <INSTANCE>
 *            The type of the instances stored in the data set specified by the
 *            generic parameter <DATASET>.
 * @param <DATASET>
 *            The type of the data set used to learn from and predict batches.
 */
public abstract class APredictiveModel<TARGETTYPE, TARGETVALUETYPE, INSTANCE, DATASET extends IDataset<INSTANCE>>
		implements IPredictiveModel<TARGETVALUETYPE, INSTANCE, DATASET> {

	/**
	 * Target type of the predicted values induced by the predicted model.
	 */
	private TARGETTYPE targetType;

	/**
	 * Getter method for the given <code>targetType</code>.
	 * 
	 * @return Returns the target type used for generating predictions
	 */
	public TARGETTYPE getTargetType() {
		return targetType;
	}

	/**
	 * Setter method for the given <code>targetType</code>.
	 * 
	 * @param targetType
	 *            The target type used for generating predictions
	 */
	public void setTargetType(TARGETTYPE targetType) {
		this.targetType = targetType;
	}
}
