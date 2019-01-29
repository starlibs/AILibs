package jaicore.ml.activelearning;

import jaicore.ml.core.dataset.IInstance;

/**
 * A strategy for selective sampling.
 * @author Jonas Hanselle
 *
 */
public interface ISelectiveSamplingStrategy {
	
	/**
	 * Chooses the {@link IInstance} to query next.
	 * @return {@link IInstance} to query next.
	 */
	public IInstance nextQueryInstance();

}
