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
	
	private double[][] DFTCoefficientsMatrix;
	
	private double[] DFTCoefficientsInstance;
	/**
	 * default value for the computation of the DFT Coefficients normally set to the wordlength/2
	 */
	private int numberOfDisieredCoefficients = 10; 
	
	/**
	 * tracks weather the number of the desired coefficients is set manually
	 */
	private boolean variableSet = false;
	
	/**
	 * tracks weather the fit method was called
	 */
	private boolean fittedInstance = false;
	private boolean fittedMatrix = false;
	private boolean fitted = false;
	
	/**
	 *  The variable is set to 1/sqrt(n) in paper "Efficient Retrieval of Similar Time Sequences Using DFT" by Davood Rafieidrafiei and Alberto Mendelzon
	 *  but in the original "The BOSS is concerned with time series classification in the presence of noise" by Patrick Schäfer
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
	public void fit(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception{
		
		if(input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		
		
		for(int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {
			fitTransform(input.getValues(matrix));
			fittedMatrix = false;
			DFTCoefficients.add(DFTCoefficientsMatrix);
		}
		
		fitted = true;
	}

	@Override
	public TimeSeriesDataset fitTransform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

	
	@Override
	public double[] transform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		if(!fitted) {
			throw new NoneFittedFilterExeception("The fit method must be called before the transform method.");
		}
		return DFTCoefficientsInstance;
	}

	@Override
	public void fit(double[] input) throws IllegalArgumentException{
		
		if(numberOfDisieredCoefficients > input.length) {
			throw new IllegalArgumentException("There cannot be more DFT coefficents calcualated than there entrys in the basis instance.");
		}
		
		if(!variableSet) {
			paperSpecificVariable = (double) 1.0/((double)input.length);
		}
		
		if(input.length == 0) {
			throw new IllegalArgumentException("The to transform instance can not be of length zero.");
		}
		//The buffer for the calculated DFT coefficeients
		DFTCoefficientsInstance = new double[numberOfDisieredCoefficients*2];
		
		//Variable used to make steps of size two in a loop that makes setps of size one
		int loopcounter = 0;
		for(int entry = 0; entry < input.length; entry++) {
			
			Complex result = new Complex(0,0);
			Complex tmp = null;
			
			for(int coefficient = 0; coefficient<numberOfDisieredCoefficients; coefficient++) {
				double currentEntry = input[entry];
				
				//calculates the real and imaginary part of the entry according to the desired coefficient
				
				double realpart = Math.cos(-(1.0/(double)input.length)*2.0*Math.PI*(double)entry*(double)coefficient);
				double imaginarypart =  Math.sin(-(1.0/(double)input.length)*2.0*Math.PI*(double)entry*(double)coefficient);
				
				tmp = new Complex(realpart,imaginarypart);
				tmp = tmp.multiply(currentEntry);
				
				result = result.add(tmp);
			}
			
			result = result.multiply(paperSpecificVariable);
			
			//saves the calculated coefficient in the buffer with first the real part and than the imaginary
			DFTCoefficientsInstance[loopcounter]= result.getReal();
			DFTCoefficientsInstance[loopcounter+1] = result.getImaginary();
			
			loopcounter+=2;
		}
		fittedInstance = true;
	}

	@Override
	public double[] fitTransform(double[] input)  throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input) ;
	}

	@Override
	public double[][] transform(double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fit(double[][] input) throws IllegalArgumentException {
		DFTCoefficientsMatrix = new double[input.length][numberOfDisieredCoefficients*2];
		double[] DFTCoefficientsOFInstance = null;
		for(int instance = 0; instance<input.length; instance++) {
			try {
				DFTCoefficientsOFInstance = fitTransform(input[instance]);
			} catch (NoneFittedFilterExeception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fittedInstance = false;
			DFTCoefficientsMatrix[instance] = DFTCoefficientsOFInstance;
		}
		
	}

	@Override
	public double[][] fitTransform(double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

}
