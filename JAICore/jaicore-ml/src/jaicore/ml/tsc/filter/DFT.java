package jaicore.ml.tsc.filter;

import java.util.ArrayList;

import org.apache.commons.math.complex.Complex;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class DFT implements IFilter {

	private ArrayList<double[][]> DFTCoefficients = new ArrayList<double[][]>();
	//TODO sinvollen wert finden 
	private int numberOfDisieredCoefficients = 10; 
	
	private boolean variableSet = false;
	private boolean fitted = false;
	
	/**
	 *  The variable is set to 1/sqrt(n) in paper "Efficient Retrieval of Similar Time Sequences Using DFT" by Davood Rafieidrafiei and Alberto Mendelzon
	 *  but in the orignal "The BOSS is concerned with time series classification in the presence of noise" by Patrick Sch�fer
	 *  it is set to 1/n. By default it is set to 1/n. 
	 */ 
	private double paperSpecificVariable;   
	
	public void setPaperSpecificVariable(double paperSpecificVariable) {
		this.paperSpecificVariable = paperSpecificVariable;
		variableSet = true;
	}

	public void setNumberOfDisieredCoefficients(int numberOfDisieredCoefficients) {
		this.numberOfDisieredCoefficients = numberOfDisieredCoefficients;
	}

	@Override
	public TimeSeriesDataset transform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Dataset empty ??
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}
		if(((TimeSeriesDataset)input).isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		if(!fitted) {
			throw new NoneFittedFilterExeception("The fit method must be called before the transform method is called.");
		}
		//First value is real part and second imaginary
		TimeSeriesDataset output = new TimeSeriesDataset(DFTCoefficients, null, null);
		
		return output;
	}

	@Override
	public void fit(TimeSeriesDataset input) {
		// TODO Auto-generated method stub
		
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}
		
		if(input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}

		double InstancesLength = input.getValues(0)[0].length;
		if(!variableSet) {
			paperSpecificVariable = (double) 1.0/((double)InstancesLength);
		}
		
		if(numberOfDisieredCoefficients > InstancesLength) {
			throw new IllegalArgumentException("The number of desired coeficientes must be smaller than the number of data points of an instance.");
		}
		
		for(int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {
			double[][] matrixDFTCoefficient = new double [(int) input.getNumberOfInstances()][numberOfDisieredCoefficients*2];
			for(int instances = 0; instances < input.getNumberOfInstances(); instances++) {
				int loopcounter = 0;
				for(int f = 0; f < numberOfDisieredCoefficients; f++) {	
					
					Complex result = new Complex(0,0);
					Complex c = null;
					
					for(int t = 0; t<InstancesLength; t++) {
						
						double entry = input.getValues(matrix)[instances][t];
						
						double realpart = Math.cos(-(1.0/(double)InstancesLength)*2.0*Math.PI*(double)t*(double)f);
						double imaginarypart = Math.sin(-(1.0/(double)InstancesLength)*2.0*Math.PI*(double)t*(double)f);
						
						c= new Complex(realpart, imaginarypart);
						
						c = c.multiply(entry);
						result = result.add(c);
						
					}
					//TODO faster if special cases are catched T(x)= 0
					result = result.multiply(paperSpecificVariable);
					if(Math.abs(result.getImaginary())<Math.pow(10, -15)) {
						result = new Complex(result.getReal(),0);
					}
					if(Math.abs(result.getReal())<Math.pow(10, -15)){
						result = new Complex(0,result.getImaginary());
					}
					matrixDFTCoefficient[instances][loopcounter] = result.getReal();
					matrixDFTCoefficient[instances][loopcounter+1] = result.getImaginary();
					loopcounter= loopcounter+2;
				}
			}
			DFTCoefficients.add(matrixDFTCoefficient);
		}
		fitted = true;
	}

	@Override
	public TimeSeriesDataset fitTransform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

}
