package ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.common.parallelization.IParallelizable;

/**
 * Functional interface to write custom logic for selecting the amount of strati
 * for a dataset.
 *
 * @author Lukas Brandt
 */
public interface IStratiAmountSelector<I extends ILabeledInstance, D extends ILabeledDataset<I>> extends IParallelizable {

	/**
	 * Select a suitable amount of strati for a Dataset.
	 *
	 * @param dataset
	 *            The dataset that will be stratified.
	 * @return The determined amount of strati.
	 */
	public int selectStratiAmount(D dataset);

}
