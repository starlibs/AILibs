package jaicore.ml.tsc.filter;

import java.util.ArrayList;

import org.apache.commons.math.complex.Complex;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class DFT implements IFilter {

	private ArrayList<INDArray> DFTCoefficients = null;
	//TODO sinvollen wert finden 
	private int numberOfDisieredCoefficients = 10; 
	
	private boolean fitted = false;
	
	public void setNumberOfDisieredCoefficients(int numberOfDisieredCoefficients) {
		this.numberOfDisieredCoefficients = numberOfDisieredCoefficients;
	}

	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Dataset empty ??
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}
		if(!fitted) {
			throw new NoneFittedFilterExeception("The fit method must be called before the transform method is called.");
		}
		//First value is real part and second imaginary
		TimeSeriesDataset output = new TimeSeriesDataset(DFTCoefficients, null, null);
		return output;
	}

	@Override
	public void fit(IDataset input) {
		// TODO Auto-generated method stub
		
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}
		
		//TODO Dataset empty ??
		int InstancesLength = (int) ((TimeSeriesDataset) input).getValues(0).getRow(0).length();
		
		if(numberOfDisieredCoefficients > InstancesLength) {
			throw new IllegalArgumentException("The number of desired coeficientes must be smaller than the number of data points of an instance.");
		}
		
		for(int matrix = 0; matrix < ((TimeSeriesDataset) input).getNumberOfVariables(); matrix++) {
			INDArray matrixDFTCoefficient = Nd4j.create(new long [] {((TimeSeriesDataset) input).getNumberOfInstances(),numberOfDisieredCoefficients*2});
			for(int instances = 0; instances < ((TimeSeriesDataset) input).getNumberOfInstances(); instances++) {
				
				for(int f = 0; f < numberOfDisieredCoefficients; f++) {	
					int loopcounter = 0;
					Complex c = null;
					for(int t = 0; t<InstancesLength; t++) {
						
						double entry = ((TimeSeriesDataset) input).getValues(matrix).getRow(instances).getDouble(t);
						
						//TODO can not find a exponential function for Nd4j that is free to use 
						c= new Complex(Math.cos(-(1/InstancesLength-1)*2*Math.PI*t*f), Math.sin(-(1/InstancesLength-1)*2*Math.PI*t*f));
						c.multiply(entry);
						
					}
					c.multiply(1/Math.sqrt(InstancesLength));
					matrixDFTCoefficient.putScalar(new long[] {instances,loopcounter},c.getReal());
					matrixDFTCoefficient.putScalar(new long[] {instances,loopcounter+1}, c.getImaginary());
					loopcounter= loopcounter+2;
				}
			}
			
			DFTCoefficients.add(matrixDFTCoefficient);
		}
		fitted = true;
	}

	@Override
	public IDataset fitTransform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		transform(input);
		return null;
	}

}
