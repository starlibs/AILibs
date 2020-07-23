package ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.common.control.IParallelizable;

/**
 * @author Felix Mohr
 */
public interface IStratifier extends IParallelizable {

	/**
	 * Prepares the stratification technique but does not assign instances to strati.
	 *
	 * @param dataset
	 * @return The number of strati for the given dataset
	 */
	public int createStrati(IDataset<?> dataset);

	/**
	 * Determines to which stratum this instance belongs
	 *
	 * @param instance
	 * @return id of stratum
	 */
	public int getStratum(IInstance instance);

}
