package jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * Interface to write custom Assigner for datapoints to strati.
 * 
 * @author Lukas Brandt
 */
public interface IStratiAssigner <I extends IInstance> {

	
	/**
	 * Initialize custom assigner if necessary.
	 * @param dataset The dataset the datapoints will be sampled from.
	 * @param stratiAmount The predetermined amount of strati the dataset will be stratified into.
	 */
	public void init(IDataset<I> dataset, int stratiAmount);
	
	/**
	 * Custom logic for assigning datapoints into strati.
	 * @param datapoint The datapoint that has to be assigned.
	 * @return The index of the strati the datapoint will be assigned into.
	 */
	public int assignToStrati(IInstance datapoint);
	
	/**
	 * Sets the number of CPU cores that can be used for parallel computation
	 * 
	 * @param numberOfCPUs
	 */
	public void setNumCPUs(int numberOfCPUs);

	public int getNumCPUs();
	
	
}
