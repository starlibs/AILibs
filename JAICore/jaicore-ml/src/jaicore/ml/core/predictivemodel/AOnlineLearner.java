package jaicore.ml.core.predictivemodel;

/**
 * Abstract extension of {@link IOnlineLearner} to be able to construct
 * prediction of the given <TARGET> type.
 *
 * @author Julian Lienen
 *
 * @param <TARGET>
 *            The type of the target that this {@link AOnlineLearner} predicts.
 */
public abstract class AOnlineLearner<TARGET> implements IOnlineLearner<TARGET> {

}
