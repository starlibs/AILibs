package ai.libs.jaicore.ml.core.predictivemodel;

import org.api4.java.ai.ml.algorithm.predictivemodel.IBatchLearner;
import org.api4.java.ai.ml.core.dataset.IDataset;

/**
 * Abstract extension of {@link IBatchLearner} to be able to construct
 * prediction of the given <T> type.
 *
 * @author Julian Lienen
 *
 * @param <T>
 *            The attribute type of the target that this {@link ABatchLearner} predicts.
 * @param <V>
 *            The value type of the target that this {@link ABatchLearner} predicts.
 * @param <I>
 *            The type of the instances stored in the data set specified by the generic parameter <D>.
 * @param <D>
 *            The type of the data set used to learn from and predict batches.
 */
public abstract class ABatchLearner<T, V, I, D extends IDataset<I>> extends APredictiveModel<T, V, I, D> implements IBatchLearner<V, I, D> {

}
