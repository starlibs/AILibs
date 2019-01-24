package jaicore.ml.tsc.filter;

import java.util.ArrayList;

import org.apache.commons.math.complex.Complex;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

/**
 * @author Helen Beierling
 * 
 *	Rafiei, D., and Mendelzon, A. Efficient retrieval of similar time sequences using DFT.
 *	(1998), pp. 249–257. (1)
 *
 *	Schäfer, P.: The BOSS is concerned with time series classification in the presence of noise. DMKD (2015)
 *	p.1510 (2)
 */
public class DFT implements IFilter {

	/**
	 * Is used to save the final DFT Coefficients matrices. Each entry in the list corresponds to one 
	 * matrix in the original dataset.
	 */
	private ArrayList<double[][]> DFTCoefficients = new ArrayList<double[][]>();
	//TODO sinvollen wert finden 
	
	
	/**
	 * default value for the computation of the DFT Coefficients normally set to the wordlength/2
	 */
	private int numberOfDisieredCoefficients = 10; 
	
	/**
	 * tracks weather the number of the desired coefficients is set manually
	 */
	private boolean variableSet = false;
	
	/**
	 * tracks weather the fitt mehtod was called
	 */
	private boolean fitted = false;
	
	/**
	 *  The variable is set to 1/sqrt(n) in paper "Efficient Retrieval of Similar Time Sequences Using DFT" by Davood Rafieidrafiei and Alberto Mendelzon
	 *  but in the original "The BOSS is concerned with time series classification in the presence of noise" by Patrick Schï¿½fer
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

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#transform(jaicore.ml.tsc.dataset.TimeSeriesDataset)
	 * 
	 * Returns a new  DFT dataset according to the by fit calculated DFT coefficents.
	 *
	 */
	@Override
	public TimeSeriesDataset transform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		
		if(input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		
		if(!fitted) {
			throw new NoneFittedFilterExeception("The fit method must be called before the transform method is called.");
		}
		//creates a new dataset out of the matrix vice Arraylist of the DFT coefficents calculated by fit
		TimeSeriesDataset output = new TimeSeriesDataset(DFTCoefficients, null, null);
		
		return output;
	}

	//calculates the number of desired DFT coefficients for each instance 
	@Override
	public void fit(TimeSeriesDataset input) {
		
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
		
		// go over every matrix in the dataset (if multivirat)
		for(int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {
			// matrix to save the calculated DFT vlaues one for every dataset matrix (instances x desired coefficients*2)(real and imaginary pro coefficient)
			double[][] matrixDFTCoefficient = new double [(int) input.getNumberOfInstances()][numberOfDisieredCoefficients*2];
			for(int instances = 0; instances < input.getNumberOfInstances(); instances++) {
				// used to make stepsize of two in an one stepsize loop 
				int loopcounter = 0;
				for(int f = 0; f < numberOfDisieredCoefficients; f++) {	
					
					Complex result = new Complex(0,0);
					Complex c = null;
					//quelle (2) formular 4 
					for(int t = 0; t<InstancesLength; t++) {
						
						double entry = input.getValues(matrix)[instances][t];
						
						double realpart = Math.cos(-(1.0/(double)InstancesLength)*2.0*Math.PI*(double)t*(double)f);
						double imaginarypart = Math.sin(-(1.0/(double)InstancesLength)*2.0*Math.PI*(double)t*(double)f);
						
						c= new Complex(realpart, imaginarypart);
						
						c = c.multiply(entry);
						result = result.add(c);
						
					}
					
					result = result.multiply(paperSpecificVariable);
					// tries to fix the inaccuracy of the double datatype
					if(Math.abs(result.getImaginary())<Math.pow(10, -15)) {
						result = new Complex(result.getReal(),0);
					}
					if(Math.abs(result.getReal())<Math.pow(10, -15)){
						result = new Complex(0,result.getImaginary());
					}
					// fills the DFT matrix for the instance
					matrixDFTCoefficient[instances][loopcounter] = result.getReal();
					matrixDFTCoefficient[instances][loopcounter+1] = result.getImaginary();
					loopcounter= loopcounter+2;
				}
			}
			// fills the later dataset for the matrix
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
