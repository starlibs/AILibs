package jaicore.ml.core.dataset.sampling.stratified.sampling;

import jaicore.ml.core.dataset.IDataset;

/**
 * Functional interface to write custom logic for selecting the amount of strati for a dataset.
 * 
 * @author Lukas Brandt
 */
@FunctionalInterface
public interface IStratiAmountSelector {

	/**
	 * Select a suitable amount of strati for a Dataset.
	 * @param dataset The dataset that will be stratified.
	 * @return The determined amount of strati.
	 */
	public int selectStratiAmount(IDataset dataset);
	
}
