package jaicore.ml.core.predictivemodel;

import jaicore.ml.core.dataset.IDataset;

/**
 * Abstract extension of {@link IBatchLearner} to be able to construct
 * prediction of the given <TARGET> type.
 * 
 * @author Julian Lienen
 *
 * @param <TARGETTYPE>
 *            The attribute type of the target that this {@link ABatchLearner}
 *            predicts.
 * @param <TARGETVALUETYPE>
 *            The value type of the target that this {@link ABatchLearner}
 *            predicts.
 * @param <INSTANCE>
 *            The type of the instances stored in the data set specified by the
 *            generic parameter <DATASET>.
 * @param <DATASET>
 *            The type of the data set used to learn from and predict batches.
 */
public abstract class ABatchLearner<TARGETTYPE, TARGETVALUETYPE, INSTANCE, DATASET extends IDataset<INSTANCE>>
		extends APredictiveModel<TARGETTYPE, TARGETVALUETYPE, INSTANCE, DATASET>
		implements IBatchLearner<TARGETVALUETYPE, INSTANCE, DATASET> {

}
