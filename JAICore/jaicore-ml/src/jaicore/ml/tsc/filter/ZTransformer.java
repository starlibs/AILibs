/**
 * 
 */
package jaicore.ml.tsc.filter;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;


/**
 * @author Helen Beierling
 *
 */
public class ZTransformer implements IFilter {
	
	private INDArray means;
	private INDArray deviation;
	private boolean fitted = false;

	
	//TODO all methods static ? 
	
	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#transform(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception{
		
	//TODO is a dataset empty if it has no attributes
		
//		if(input.) {
//			throw new IllegalArgumentException("The input dataset was empty");
//		}
		
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports timeseries datasets");
		}
		if(fitted) {
			
			for(int i = 0; i < ((TimeSeriesDataset) input).getNumberOfVariables(); i++){			
					 INDArray matrix =  ((TimeSeriesDataset) input).getValues(i);
					 for(int instance = 0; instance < ((TimeSeriesDataset) input).getNumberOfInstances(); instance++) {
						INDArray row =  matrix.getRow(instance);
						for(int elem = 0; elem < row.length(); elem++) {
							//update every elem by the calculation of elem multiplied by the mean of the according instance
							row.putScalar(elem, ((row.getDouble(elem)*means.getDouble(new int [] {i,instance}))/deviation.getDouble(new int[]{i,instance})));
						}
						matrix.putRow(instance, row);
					 }
				((TimeSeriesDataset) input).replace(i,matrix,null);
			}
		}else {
			throw new NoneFittedFilterExeception("The fit method must be called before the transform method.");
		}
		
		return input;
	}

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fit(jaicore.ml.core.dataset.IDataset)
	 */
	
	@Override
	public void fit(IDataset input) {
		
		if(!(input instanceof TimeSeriesDataset)){
			throw new IllegalArgumentException("This mehtod only supports for timeseries datasets.");
		}	
		
		//make suitable means and deviation matrix rows == different attributes columns == different instances
		means = Nd4j.zeros(((TimeSeriesDataset) input).getNumberOfVariables(), ((TimeSeriesDataset) input).getNumberOfInstances()); 
		deviation = Nd4j.zeros(((TimeSeriesDataset) input).getNumberOfVariables(),((TimeSeriesDataset) input).getNumberOfInstances());
		
		//for every attribute for every instance of this attribute compute mean and deviation and put it in the according cell in matrix 
		for(int matrix = 0; matrix < ((TimeSeriesDataset) input).getNumberOfVariables(); matrix++) {
			for(int row = 0; row< ((TimeSeriesDataset) input).getNumberOfInstances();row++) {
				int [] index = {matrix, row};
				means.putScalar(index,(double)((TimeSeriesDataset) input).getValues(matrix).getRow(row).mean(1).getDouble(0));
				deviation.putScalar(index, (double)((TimeSeriesDataset) input).getValues(matrix).getRow(row).std(1).getDouble(0));
			}		
		}
		
		fitted = true;
	}


	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fitTransform(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public IDataset fitTransform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		//TODO call fit in transform or not ?
		fit(input);
		fitted = true;
		return transform(input);
	}

}
