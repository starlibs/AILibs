package jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.Arrays;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

/**
 * @author Helen Beierling
 * This class cuts an instance or a set of instances into a number of smaller instances which are
 * typically saved in an matrix per instance and the matrices in a list. 
 * c.f. p.1508 "The BOSS is concerned with time series classification in the presence of noise" by Patrick Schäfer
 */
public class SlidingWindowBuilder implements IFilter{
	
	private boolean fitted = false;
	private boolean fittedMatrix = false;
	
	
	//TODO find meaningfull value
	private int defaultWindowSize = 20;
	
	private ArrayList<double[][]> blownUpDataset = new ArrayList<double[][]>();
	private double [][] blownUpMatrix = null;

	public void setDefaultWindowSize(int defaultWindowSize) {
		this.defaultWindowSize = defaultWindowSize;
	}
	
	public int getDefaultWindowSize() {
		return defaultWindowSize;
	}
	
	@Override
	public TimeSeriesDataset transform(TimeSeriesDataset input)
			throws IllegalArgumentException, NoneFittedFilterExeception {
		if(input.isEmpty()) {
			throw new IllegalArgumentException("The input dataset can not be empty");
		}
		
		if(!fitted){
			throw new NoneFittedFilterExeception("The fit mehtod must be called before transformning");
		}
		
		return new TimeSeriesDataset(blownUpDataset,null,null);
	}

	@Override
	// Results in a list of matrices where each instance has its own matrix.
	// Therefore the structure of the matrices are lost if this method is used.
	public void fit(TimeSeriesDataset input) {
		for(int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {
			ArrayList<double[][]> newMatrices = new ArrayList<double[][]>();
			for(double [] instance : input.getValues(matrix)) {	
				double[][] newMatrix = new double[(instance.length-defaultWindowSize)][defaultWindowSize];
				for(int entry = 0; entry <  instance.length-defaultWindowSize; entry++) {
				   double[] tmp = Arrays.copyOfRange(instance, entry, entry+defaultWindowSize);
				   newMatrix[entry] = tmp;
				}
				newMatrices.add(newMatrix);
			}
			
			blownUpDataset = newMatrices;
		}
		fitted = true;
	}
	
	
	/**
	 * This is an extra fit method because it does not return a double[] array even though it gets
	 * a double [] as input as it would be defined in the .  
	 * @param 	instance that has to be transformed
	 * @return 	the tsdataset that results from one instance which consists of 
	 * 			one matrix with each row represents one part of the instance from i to i+ window length for i < n- window length
	 */
	public TimeSeriesDataset specialFitTransform(double[] instance){
		if(instance.length==0) {
			throw new IllegalArgumentException("The input instance can not be empty");
		}
		if(instance.length < defaultWindowSize) {
			throw new IllegalArgumentException("The input instance can not be smaller than the windowsize");
		}
		
		double [][] newMatrix = new double[instance.length-defaultWindowSize+1][defaultWindowSize];
		
		for(int entry = 0; entry <= instance.length-(defaultWindowSize); entry++) {
			newMatrix[entry] = Arrays.copyOfRange(instance, entry, entry+defaultWindowSize);
		}
		ArrayList<double[][]> newDataset= new ArrayList<double[][]>();
		newDataset.add(newMatrix);
		TimeSeriesDataset blownUpInstance = new TimeSeriesDataset(newDataset);
		
		return blownUpInstance;
	}
	
	@Override
	public TimeSeriesDataset fitTransform(TimeSeriesDataset input)
			throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

	/*
	 * This operation is unsupported because it would result in one stream of new instances in one array.
	 */
	@Override
	public double[] transform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		throw new UnsupportedOperationException("This is done by the special fit and transform because this mehtod must return a new dataset not a double array.");
	}
	
	/*
	 * This method is unsupported because the corresponding transform operation is
	 * not useful
	 */
	@Override
	public void fit(double[] input) throws IllegalArgumentException {
		throw new UnsupportedOperationException("This is done by the special fit and transform because this mehtod must return a new dataset not a double array.");
		
	}

	@Override
	public double[] fitTransform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {	
		throw new UnsupportedOperationException("This is done by the special fit and transform because this mehtod must return a new dataset not a double array.");
		
	}

	@Override
	public double[][] transform(double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		if(input.length == 0) {
			throw new IllegalArgumentException("The input matrix can not be empty");
		}
		
		if(!fittedMatrix){
			throw new NoneFittedFilterExeception("The fit mehtod must be called before transformning");
		}
		
		return blownUpMatrix;
	}

	@Override
	//Does not return a list of matrices but a bigger matrix where the new created instances are getting stacked
	// if there is a instance of size n than the first n-window length rows are the sliced instance. 
	public void fit(double[][] input) throws IllegalArgumentException {
		if(input.length == 0) {
			throw new IllegalArgumentException("The input matrix can not be empty");
		}
		
		//This is the buffer for the new matrix that gets created from a single instance.
		 blownUpMatrix = new double[input.length*(input[0].length-defaultWindowSize)][defaultWindowSize];
		for(int instance = 0; instance < input.length; instance++) {
			for(int entry = 0; entry < input[instance].length -defaultWindowSize; entry++) {
				// Every entry in the new matrix is equal to a copy of the original instance from 
				// entry i to entry i plus window length.
				blownUpMatrix[instance+(entry)] = Arrays.copyOfRange(input[instance],entry,entry+defaultWindowSize);  
			}
		}
		fittedMatrix = true;
	}

	@Override
	public double[][] fitTransform(double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

}
