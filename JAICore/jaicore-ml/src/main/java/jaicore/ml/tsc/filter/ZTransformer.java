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
 *	This class normalizes the mean of an instance to be zero and the deviation to be one. 
 *	s.https://jmotif.github.io/sax-vsm_site/morea/algorithm/znorm.html
 * 	one loop: https://www.strchr.com/standard_deviation_in_one_pass?allcomments=1
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
			
			ztransformedDataset.add(fitTransform(input.getValues(matrix)));
			fittedMatrix = false;
		}
		fitted = false;
		return new TimeSeriesDataset(ztransformedDataset);
	}

	
	@Override
	public void fit(TimeSeriesDataset input) throws IllegalArgumentException{
			
		if(input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		
		fitted = true;
	}


	@Override
	public TimeSeriesDataset fitTransform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}
	
	@Override
	public double[] transform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		if(!fittedInstance) {
			throw new NoneFittedFilterExeception("The fit method must be called before the transfom method is called");
		}
		if(input.length == 0) {
			throw new IllegalArgumentException("The to transform instance can not be empty");
		}
		
		double[] ztransform = new double[input.length];
		for(int entry = 0; entry < input.length; entry++) {
			if(deviation != 0) {
				ztransform[entry] = (entry-mean)/deviation;
			}
		}
		fittedInstance = false;
		return ztransform;
	}
	
	@Override
	public void fit(double[] input) throws IllegalArgumentException {
		double SumSq = 0.0;
		double SumMean = 0.0;
		double NumberEntrys = input.length;
		
		if(NumberEntrys == 0) {
			throw new IllegalArgumentException("The to transform instance can not be empty.");
		}
		//TODO can be numerical inaccurate if the data is large
		for(int entry = 0; entry<input.length;entry++) {
			SumSq = SumSq + Math.pow(input[entry],2);
			SumMean = SumMean + input[entry];
		}
		mean = SumMean/NumberEntrys;
		double variance = (1/NumberEntrys)*(SumSq)-Math.pow(mean, 2);
		if(BasselCorrected) {
			double tmp = (NumberEntrys/(NumberEntrys-1));
			deviation = Math.sqrt(tmp*variance);
		}
		else {
			
			deviation =  Math.sqrt(variance);
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
		fittedMatrix = false;
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
