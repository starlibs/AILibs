package ai.libs.jaicore.ml.core.predictivemodel;

import org.api4.java.ai.ml.IDataset;

/**
 * Abstract extension of {@link IOnlineLearner} to be able to construct
 * prediction of the given <T> type.
 *
 * @author Julian Lienen
 *
 * @param <T>
 *            The attribute type of the target that this {@link AOnlineLearner} predicts.
 * @param <V>
 *            The value type of the target that this {@link AOnlineLearner} predicts.
 * @param <I>
 *            The type of the instances stored in the data set specified by the generic parameter <D>.
 * @param <D>
 *            The type of the data set used to learn from and predict batches.
 */
public abstract class AOnlineLearner<T, V, I, D extends IDataset<I>> extends ABatchLearner<T, V, I, D> implements IOnlineLearner<V, I, D> {

}
