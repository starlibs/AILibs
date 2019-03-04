package jaicore.ml.activelearning;

import java.util.Collection;

import jaicore.ml.core.dataset.IInstance;

/**
 * Provides a sample pool for pool-based active learning.
 * @author Jonas Hanselle
 *
 */
public interface IActiveLearningPoolProvider {
	
	/**
	 * Returns the pool of unlabeled instances.
	 * @return Pool of unlabeled instances.
	 */
	public Collection<IInstance> getPool();
	
	/**
	 * Labels the given instance.
	 * @param queryInstance {@link IInstance} to be labeled.
	 * @return Labeled {@link IInstance}.
	 */
	public IInstance query(IInstance queryInstance);

}
