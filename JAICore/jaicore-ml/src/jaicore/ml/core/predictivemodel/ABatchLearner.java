package jaicore.ml.core.predictivemodel;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * Abstract extension of {@link IBatchLearner} to be able to construct
 * prediction of the given <TARGET> type.
 * 
 * @author Julian Lienen
 *
 * @param <TARGET>
 *            The type of the target that this {@link ABatchLearner} predicts.
 * @param <INSTANCE>
 *            The type of the instances stored in the data set specified by the
 *            generic parameter <DATASET>.
 * @param <DATASET>
 *            The type of the data set used to learn from and predict batches.
 */
public abstract class ABatchLearner<TARGET, INSTANCE extends IInstance, DATASET extends IDataset<INSTANCE>>
		extends APredictiveModel<TARGET, INSTANCE, DATASET> implements IBatchLearner<TARGET, INSTANCE, DATASET> {

}
