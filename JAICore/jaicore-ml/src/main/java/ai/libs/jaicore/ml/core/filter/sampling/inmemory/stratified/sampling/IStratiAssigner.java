package ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.common.control.IParallelizable;

/**
 * Interface to write custom Assigner for datapoints to strati.
 *
 * @author Lukas Brandt
 */
public interface IStratiAssigner extends IParallelizable {

	/**
	 * Initialize custom assigner if necessary.
	 * @param dataset The dataset the datapoints will be sampled from.
	 * @param stratiAmount The predetermined amount of strati the dataset will be stratified into.
	 */
	public void init(IDataset<?> dataset, int stratiAmount);

	/**
	 * Custom logic for assigning datapoints into strati.
	 * @param datapoint The datapoint that has to be assigned.
	 * @return The index of the strati the datapoint will be assigned into.
	 */
	public int assignToStrati(IInstance datapoint);

}
