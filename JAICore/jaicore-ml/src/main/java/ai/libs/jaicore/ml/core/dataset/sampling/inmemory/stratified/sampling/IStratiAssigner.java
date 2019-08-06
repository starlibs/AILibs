package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;
import org.api4.java.common.parallelization.IParallelizable;

/**
 * Interface to write custom Assigner for datapoints to strati.
 *
 * @author Lukas Brandt
 */
public interface IStratiAssigner<X, Y, I extends IFeatureInstance<X> & ILabeledInstance<Y>, D extends ISupervisedDataset<X, Y, I>> extends IParallelizable {

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
