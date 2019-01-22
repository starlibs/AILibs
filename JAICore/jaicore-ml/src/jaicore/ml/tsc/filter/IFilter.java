package jaicore.ml.tsc.filter;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public interface IFilter {
	
	/** represents a function working on a dataset by transforming the dataset itself.
	 * @param input the data set that is to transform 
	 * @return the transformt dataset 
	 * @throws NoneFittedFilterExeception  used if transform is called without fit
	 * @throws IllegalArgumentException 	used if dataset to transform is empty
	 */
	public TimeSeriesDataset transform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception;
	
	/** the function computes the needed information for the transform function.
	 * @param input the dataset that is to transform 
	 */
	public void fit(TimeSeriesDataset input);
	
	/**	a utility function to avoid the added effort of calling the fit and transform function separate
	 * @param input the dataset that is to be transfromed 
	 * @return the transformed dataset
	 * @throws NoneFittedFilterExeception  	used if transform is called without fit
	 * @throws IllegalArgumentException 	used if dataset to transform is empty
	 */
	public TimeSeriesDataset fitTransform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception;
}
