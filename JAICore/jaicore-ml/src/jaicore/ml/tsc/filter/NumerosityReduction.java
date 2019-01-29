package jaicore.ml.tsc.filter;

import java.util.ArrayList;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class NumerosityReduction implements IFilter {

	private ArrayList<double[][]> reducedDataset = new ArrayList<double[][]>();
	
	@Override
	public TimeSeriesDataset transform(TimeSeriesDataset input)
			throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fit(TimeSeriesDataset input) {
		// TODO Auto-generated method stub
		if(input.isEmpty()) {
			throw new IllegalArgumentException("The input dataset can not be empty.");
		}
		
		
	}

	@Override
	public TimeSeriesDataset fitTransform(TimeSeriesDataset input)
			throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

}
