package jaicore.ml.tsc.filter;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public interface IFilter {
	
	/** represents a function working on a dataset by transforming the dataset itself.
	 * @param input the data set that is to transform 
	 * @return the transformt dataset 
	 * @throws NoneFittedFilterExeception  used if transform is called without fit
	 * @throws IllegalArgumentException 	used if dataset to transform is empty
	 */
	public IDataset transform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception;
	
	/** the function computes the needed information for the transform function.
	 * @param input the dataset that is to transform 
	 */
	public void fit(IDataset input);
	
	/**	a utility function to avoid the added effort of calling the fit and transform function separate
	 * @param input the dataset that is to be transfromed 
	 * @return the transformed dataset
	 * @throws NoneFittedFilterExeception  	used if transform is called without fit
	 * @throws IllegalArgumentException 	used if dataset to transform is empty
	 */
	public IDataset fitTransform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception;
}
