package jaicore.ml.core.predictivemodel;

/**
 * Abstract extension of {@link IBatchLearner} to be able to construct
 * prediction of the given <TARGET> type.
 * 
 * @author Julian Lienen
 *
 * @param <TARGET>
 *            The type of the target that this {@link ABatchLearner} predicts.
 */
public abstract class ABatchLearner<TARGET> implements IBatchLearner<TARGET> {

}
