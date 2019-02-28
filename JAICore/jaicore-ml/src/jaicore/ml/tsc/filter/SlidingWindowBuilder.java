package jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.Arrays;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class SlidingWindowBuilder implements IFilter{
	
	private boolean fitted = false;
	private boolean fittedInstance = false;
	
	//TODO find meaningfull value
	private int defaultWindowSize = 20;
	private ArrayList<double[][]> blownUpDataset = new ArrayList<double[][]>();

	public void setDefaultWindowSize(int defaultWindowSize) {
		this.defaultWindowSize = defaultWindowSize;
	}
	
	public int getDefaultWindowSize() {
		return defaultWindowSize;
	}
	
	@Override
	public TimeSeriesDataset transform(TimeSeriesDataset input)
			throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
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
			for(int instance = 0; instance < input.getNumberOfInstances(); instance++) {
				for(int entry = 0; entry < input.getValues(matrix)[instance].length-(defaultWindowSize); entry++) {
					newinstances.add(Arrays.copyOfRange(input.getValues(matrix)[instance], entry, entry+defaultWindowSize-1));
				}
			}
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

	@Override
	public double[] transformInstance(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fitInstance(double[] input) throws IllegalArgumentException {
		/*
		 * if(defaultWindowSize > input.length) { //TODO Or should than the instance be
		 * returned throw new
		 * IllegalArgumentException("The window length can not be greater than the instance length"
		 * ); } if(input.length == 0) { throw new
		 * IllegalArgumentException("The input can not be empty."); } double[]
		 * blownUpEntry = new double[input.length*defaultWindowSize]; for(int entry = 0;
		 * entry < input.length; entry++) {
		 * 
		 * }
		 */
	}

	@Override
	public double[] fitTransformInstance(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		fitInstance(input);
		return transformInstance(input);
	}

}
