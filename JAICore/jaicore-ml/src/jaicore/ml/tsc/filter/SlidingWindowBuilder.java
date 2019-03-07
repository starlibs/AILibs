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
		
		throw new UnsupportedOperationException("This is done by using the special fit and transform.");
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
	public double[] fitTransform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		throw new UnsupportedOperationException("This is done by using the special fit and transform.");
		/*
		 * fitInstance(input); return transformInstance(input);
		 */
	}

	@Override
	public double[][] transform(double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fit(double[][] input) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[][] fitTransform(double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		return null;
	}

}
