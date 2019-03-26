package jaicore.ml.core.predictivemodel;

import java.util.Set;

import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.TrainingException;

/**
 * The {@link IOnlineLearner} models a learning algorithm which works in an online fashion, i.e. takes either a single {@link IInstance} or a {@link Set} thereof as training input.
 * 
 * @author Alexander Hetzer
 *
 * @param <TARGET>
 *            The type of the target that this {@link IOnlineLearner} predicts.
 */
public interface IOnlineLearner<TARGET> extends IBatchLearner<TARGET> {

	/**
	 * Updates this {@link IOnlineLearner} based on the given {@link Set} of {@link IInstance}s.
	 * 
	 * @param instances
	 *            The {@link Set} of {@link IInstance}s the update should be based on.
	 * @throws TrainingException
	 *             If something fails during the update process.
	 */
	public void update(Set<IInstance> instances) throws TrainingException;

	/**
	 * Updates this {@link IOnlineLearner} based on the given {@link IInstance}.
	 * 
	 * @param instance
	 *            The {@link IInstance} the update should be based on.
	 * @throws TrainingException
	 *             If something fails during the update process.
	 */
	public void update(IInstance instance) throws TrainingException;
}
