/**
 * 
 */
package jaicore.ml.tsc.filter;

import java.util.Arrays;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;


/**
 * @author Helen Beierling
 *
 */
public class ZTransformer implements IFilter {
	
	private double [][] means;

	private double [][] deviation;

	private boolean fitted = false;

	public double[][] getMeans() {
		return means;
	}
	public double[][] getDeviation() {
		return deviation;
	}
	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#transform(jaicore.ml.core.dataset.IDataset)
	 */
	
	@Override
	public TimeSeriesDataset transform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception{
		
	//TODO is a dataset empty if it has no attributes}
		
		if(input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		if(fitted) {
			for(int i = 0; i < input.getNumberOfVariables(); i++){			
					 double[][] matrix =  input.getValues(i);
					 for(int instance = 0; instance < ((TimeSeriesDataset) input).getNumberOfInstances(); instance++) {
						double[] row =  matrix[instance];
						for(int elem = 0; elem < row.length; elem++) {
							//update every elem by the calculation of elem multiplied by the mean of the according instance
							row[elem]=((row[elem]*means[i][instance])/deviation[i][instance]);
						}
						matrix[instance]= row;
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
	public void fit(TimeSeriesDataset input) {
		
		if(!(input instanceof TimeSeriesDataset)){
			throw new IllegalArgumentException("This mehtod only supports for timeseries datasets.");
		}	
		if(input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		
		//make suitable means and deviation matrix rows == different attributes columns == different instances
		
		means = new double[input.getNumberOfVariables()][(int) input.getNumberOfInstances()];
		deviation = new double[input.getNumberOfVariables()][(int) input.getNumberOfInstances()];
		
		
		//for every attribute for every instance of this attribute compute mean and deviation and put it in the according cell in matrix 
		for(int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {
			for(int instance = 0; instance < input.getNumberOfInstances(); instance++) {
				means[matrix][instance] = Arrays.stream(input.getValues(matrix)[instance]).average().getAsDouble();
				double sum = 0;
				for( double elem : input.getValues(matrix)[instance]) {
					sum += Math.pow((elem - means[matrix][instance]),2);
				}
				deviation[matrix][instance] = Math.sqrt(1/(input.getValues(matrix)[instance].length-1)*sum);		
			}
		}
		
		fitted = true;
	}


	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fitTransform(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public TimeSeriesDataset fitTransform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

}
