package jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.Arrays;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class NumerosityReduction implements IFilter {
	
	private boolean fitted = false;
	private boolean fittedInstance = false;
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
	
	/* Deletes the duplicates in the dataset (per instance)
	 * (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fit(jaicore.ml.tsc.dataset.TimeSeriesDataset)
	 */
	@Override
	public void fit(TimeSeriesDataset input) {
		// TODO Auto-generated method stub
		if(input.isEmpty()) {
			throw new IllegalArgumentException("The input dataset can not be empty.");
		}
		// find all duplicates in the dataset are getting deleted. The whole instance in form 
		// of the SFA word gets deleted. This only happens for words that are immediate behind one another.
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
			//creates the new matrix without the deleted Instances
			double[][] reducedMatrix = new double[baseForNewMatrix.size()][input.getValues(matrix)[0].length];
			int index = 0;
			for(double[] d : reducedMatrix) {
				d=baseForNewMatrix.get(index);
				index++;
			}
			reducedDataset.add(reducedMatrix);
		}
		fitted = true;
	}

	@Override
	public TimeSeriesDataset fitTransform(TimeSeriesDataset input)
		throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

	@Override
	public double[] transform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		throw new UnsupportedOperationException("Numerity reduction is not reasonable in this context for a single Instance.");
	}

	@Override
	public void fit(double[] input) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Numerity reduction is not reasonable in this context for a single Instance.");
	}

	@Override
	public double[] fitTransform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		throw new UnsupportedOperationException("Numerity reduction is not reasonable in this context for a single Instance.");
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
