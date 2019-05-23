package jaicore.ml.activelearning;

import java.util.Collection;

import jaicore.ml.core.dataset.ILabeledInstance;

/**
 * Provides a sample pool for pool-based active learning.
 * @author Jonas Hanselle
 *
 */
public interface IActiveLearningPoolProvider<I extends ILabeledInstance> {
	
	/**
	 * Returns the pool of unlabeled instances.
	 * @return Pool of unlabeled instances.
	 */
	public Collection<I> getPool();
	
	/**
	 * Labels the given instance.
	 * @param queryInstance {@link IInstance} to be labeled.
	 * @return Labeled {@link IInstance}.
	 */
	public I query(I queryInstance);

}
