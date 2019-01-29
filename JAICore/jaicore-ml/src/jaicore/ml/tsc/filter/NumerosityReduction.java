package jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.Arrays;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class NumerosityReduction implements IFilter {
	
	private boolean fitted = false;
	private ArrayList<double[][]> reducedDataset = new ArrayList<double[][]>();
	
	@Override
	public TimeSeriesDataset transform(TimeSeriesDataset input)
			throws IllegalArgumentException, NoneFittedFilterExeception {
		if(input.isEmpty()) {
			throw new IllegalArgumentException("The to transform dataset can not be empty.");
		}
		if(!fitted) {
			throw new NoneFittedFilterExeception("The fit method must be called before transforming");
		}
		
		
		return new TimeSeriesDataset(reducedDataset,null,null);
	}

	@Override
	public void fit(TimeSeriesDataset input) {
		// TODO Auto-generated method stub
		if(input.isEmpty()) {
			throw new IllegalArgumentException("The input dataset can not be empty.");
		}
		// find all duplicates in the 
		for(int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {
			
			ArrayList<double[]> baseForNewMatrix = new ArrayList<double[]>();
			double[] word = input.getValues(matrix)[0];
			baseForNewMatrix.add(word);
			
			for(int instance = 0; instance < input.getNumberOfInstances(); instance++) {
				if(!(Arrays.equals(word, input.getValues(matrix)[instance]))) {
					word = input.getValues(matrix)[instance];
					baseForNewMatrix.add(word);
				}
			}
			
			double[][] reducedMatrix = new double[baseForNewMatrix.size()][input.getValues(matrix)[0].length];
			int index = 0;
			for(double[] d : reducedMatrix) {
				d=baseForNewMatrix.get(index);
				index++;
			}
			reducedDataset.add(reducedMatrix);
		}
	}

	@Override
	public TimeSeriesDataset fitTransform(TimeSeriesDataset input)
			throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

}
