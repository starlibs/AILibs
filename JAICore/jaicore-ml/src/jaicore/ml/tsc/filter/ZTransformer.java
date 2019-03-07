/**
 * 
 */
package jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.Arrays;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;


/**
 * @author Helen Beierling
 *
 */
public class ZTransformer implements IFilter {
	
	

	private double mean;
	private double deviation;
	private ArrayList <double[][]> ztransformedDataset = new ArrayList<double[][]>();
	private double[][] ztransformedMatrix;
	
	//To get a unbiased estimate for the variance the intermediated results are 
	//divided by n-1 instead of n(Number of samples of Population)
	private boolean BasselCorrected = true; 
	
	private boolean fitted = false;
	private boolean fittedInstance = false;
	private boolean fittedMatrix = false;

	
	public void setBasselCorrected(boolean basselCorrected) {
		BasselCorrected = basselCorrected;
	}
	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#transform(jaicore.ml.core.dataset.IDataset)
	 */
	
	@Override
	public TimeSeriesDataset transform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception{
		
	//TODO is a dataset empty if it has no attributes ?
		
		if(input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		if(!fitted) {
			throw new NoneFittedFilterExeception("The fit method must be called before the transform method.");
		}
		
		for(int matrix = 0; matrix < input.getNumberOfVariables(); matrix++){			
			fitTransform(input.getValues(matrix));
			ztransformedDataset.add(ztransformedMatrix);
			fittedMatrix = false;
		}

		return new TimeSeriesDataset(ztransformedDataset);
	}

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fit(jaicore.ml.core.dataset.IDataset)
	 */
	
	@Override
	public void fit(TimeSeriesDataset input) throws IllegalArgumentException{
			
		if(input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		//TODO should be something done here? Because ztransform can be calculated over all in the transform step
		//through the fit transform of the single instance
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
	
	@Override
	public double[] transform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		if(!fitted) {
			throw new NoneFittedFilterExeception("The fit method must be called before the transfom method is called");
		}
		if(input.length == 0) {
			throw new IllegalArgumentException("The to transform instance can not be empty");
		}
		
		double[] ztransform = new double[input.length];
		for(int entry = 0; entry < input.length; entry++) {
			ztransform[entry] = (entry-mean)/deviation;
		}
		return ztransform;
	}
	
	@Override
	public void fit(double[] input) throws IllegalArgumentException {
		double SumSq = 0;
		double SumMean = 0;
		double NumberEntrys = input.length;
		
		if(NumberEntrys == 0) {
			throw new IllegalArgumentException("The to transform instance can not be empty.");
		}
		//TODO can be numarical inaccurate if the data is large
		for(int entry = 0; entry<input.length;entry++) {
			SumSq =+ Math.pow(entry,2);
			SumMean =+ entry;
		}
		mean = SumMean/NumberEntrys;
		if(BasselCorrected) {
			deviation = Math.sqrt((SumSq/NumberEntrys - Math.pow((SumMean/NumberEntrys),2))* NumberEntrys/(NumberEntrys-1));
		}
		else {
			deviation = Math.sqrt((SumSq - (Math.pow(SumMean,2)/NumberEntrys))/NumberEntrys);
		}
		
		fittedInstance = true;
	}
	@Override
	public double[] fitTransform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

	@Override
	public double[][] transform(double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		if(!fittedMatrix) {
			throw new NoneFittedFilterExeception("The fit method must be called first.");
		}
		ztransformedMatrix = new double[input.length][input[0].length];
		for(int instance = 0; instance <input.length; instance++) {
			ztransformedMatrix[instance] = fitTransform(input[instance]);
			fittedInstance = false;
		}
		return ztransformedMatrix;
	}

	@Override
	public void fit(double[][] input) throws IllegalArgumentException {
		fittedMatrix = true;
	}

	@Override
	public double[][] fitTransform(double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

}
