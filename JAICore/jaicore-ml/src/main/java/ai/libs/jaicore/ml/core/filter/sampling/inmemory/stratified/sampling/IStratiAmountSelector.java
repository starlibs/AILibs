package ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.common.control.IParallelizable;

/**
 * Functional interface to write custom logic for selecting the amount of strati
 * for a dataset.
 *
 * @author Lukas Brandt
 */
public interface IStratiAmountSelector extends IParallelizable {

	/**
	 * Select a suitable amount of strati for a Dataset.
	 *
	 * @param dataset
	 *            The dataset that will be stratified.
	 * @return The determined amount of strati.
	 */
	public int selectStratiAmount(IDataset<?> dataset);

}
