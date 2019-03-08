package jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.Arrays;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class SlidingWindowBuilder implements IFilter{
	
	private boolean fitted = false;
	private boolean fittedMatrix = false;
	//private boolean fittedInstance = false;
	
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
	public void fit(TimeSeriesDataset input) {
		for(int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {
			ArrayList<double[]> newinstances = new ArrayList<double[]>();
			
			double[][] newMatrix = new double[newinstances.size()][defaultWindowSize];
			blownUpDataset.add(newMatrix);
		}
		fitted = true;
	}
	
	
	public TimeSeriesDataset specialFitTransform(double[] instance){
		if(instance.length==0) {
			throw new IllegalArgumentException("The input instance can not be empty");
		}
		if(instance.length < defaultWindowSize) {
			throw new IllegalArgumentException("The input instance can not be smaller than the windowsize");
		}
		
		double [][] newMatrix = new double[instance.length][defaultWindowSize];
		
		for(int entry = 0; entry < instance.length-(defaultWindowSize); entry++) {
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
		// TODO Auto-generated method stub
		fit(input);
		return transform(input);
	}

	/*
	 * This operation is unsuported because it would result in one stream of new instances in one array.
	 */
	@Override
	public double[] transform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		throw new UnsupportedOperationException("This is done by the special fit and transform because this mehtod must return a new dataset not a double array.");
	}
	
	/*
	 * This method is unsupported because the corresponding transform operation is
	 * not usefull
	 */
	@Override
	public void fit(double[] input) throws IllegalArgumentException {
		throw new UnsupportedOperationException("This is done by the special fit and transform because this mehtod must return a new dataset not a double array.");
		/*
		 * if(defaultWindowSize > input.length) { throw new
		 * IllegalArgumentException("The window length can not be greater than the instance length"
		 * ); } if(input.length == 0) { throw new
		 * IllegalArgumentException("The input can not be empty."); }
		 * 
		 * double[][] newMatrix = new
		 * double[input.length-(defaultWindowSize)][defaultWindowSize];
		 * 
		 * for(int entry = 0; entry < input.length-(defaultWindowSize); entry++) {
		 * newMatrix[entry] = Arrays.copyOfRange(input, entry,
		 * entry+(defaultWindowSize-1)); }
		 * 
		 * fittedInstance = true;
		 */
		
	}

	@Override
	public double[] fitTransform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {	
		throw new UnsupportedOperationException("This is done by the special fit and transform because this mehtod must return a new dataset not a double array.");
		/*
		 * fit(input); return transform(input);
		 */
		
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
	public void fit(double[][] input) throws IllegalArgumentException {
		if(input.length == 0) {
			throw new IllegalArgumentException("The input matrix can not be empty");
		}
		 blownUpMatrix = new double[input.length*(input[0].length-defaultWindowSize)*defaultWindowSize][defaultWindowSize];
		for(int instance = 0; instance < input.length; instance++) {
			for(int entry = 0; entry < input[instance].length -defaultWindowSize; entry++) {
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
