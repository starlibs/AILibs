package jaicore.ml.tsc.filter;

import java.util.ArrayList;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class DFT implements IFilter {

	private ArrayList<INDArray> DFTCoefficients;
	
	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}
		
		
		return null;
	}

	@Override
	public void fit(IDataset input) {
		// TODO Auto-generated method stub
		
		int InstancesLength = (int) ((TimeSeriesDataset) input).getValues(0).getRow(0).length();
		for(int matrix = 0; matrix < ((TimeSeriesDataset) input).getNumberOfVariables(); matrix++) {
			INDArray matrixDFTCoefficient = Nd4j.create(new long [] {((TimeSeriesDataset) input).getNumberOfInstances(),InstancesLength,2});
			for(int instances = 0; instances < ((TimeSeriesDataset) input).getNumberOfInstances(); instances++) {
				for(int elem = 0; elem < InstancesLength; elem++) {
					double entry = ((TimeSeriesDataset) input).getValues(matrix).getRow(instances).getDouble(elem);
					for(int t = 0; t<InstancesLength-1; t++) {
						//TODO can not find a exponential function for Nd4j that is free to use 
						entry*Math.exp(-entry)
					}
					matrixDFTCoefficient.putScalar(elem,) Nd4j.
				}
			}
		}
		
	}

	@Override
	public IDataset fitTransform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		return null;
	}

}
