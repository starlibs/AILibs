package ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.common.parallelization.IParallelizable;

/**
 * Interface to write custom Assigner for datapoints to strati.
 *
 * @author Lukas Brandt
 */
public interface IStratiAssigner<I extends ILabeledInstance, D extends ILabeledDataset<I>> extends IParallelizable {

	/**
	 * Initialize custom assigner if necessary.
	 * @param dataset The dataset the datapoints will be sampled from.
	 * @param stratiAmount The predetermined amount of strati the dataset will be stratified into.
	 */
	public void init(D dataset, int stratiAmount);

	/**
	 * Custom logic for assigning datapoints into strati.
	 * @param datapoint The datapoint that has to be assigned.
	 * @return The index of the strati the datapoint will be assigned into.
	 */
	public int assignToStrati(I datapoint);

}
