package jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.Arrays;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

/**
 * @author Helen Beierling
 *  c.f. p. 1511 p. 1510 "The BOSS is concerned with time series classification in the presence of noise" by Patrick Schäfer
 *	This class combines the MCB of finding the bins for a given set of DFT coefficients and SFA
 *	which selects the right letter for a DFT coefficient.
 */
public class SFA implements IFilter {

	private double[] alphabet;
	
	private boolean meanCorrected;
	
	private boolean fitted = false;
	private boolean fittedMatrix = false;
	
	private TimeSeriesDataset dFTDataset = null;
	private double[][] dftMatrix = null;
	
	private int numberOfDesieredDFTCoefficients;
	
	private DFT dftFilter = null;
	private DFT dftFilterMatrix = null;

	
	private ArrayList<double[][]> lookupTable = new ArrayList<double[][]>();
	private double[][] lookUpTableMatrix = null;
	
	private ArrayList<double[][]> sfaDataset = new ArrayList<double[][]>();
	private double[][] sfaMatrix = null;

	private boolean rekursiv;
	
	public void setNumberOfDesieredDFTCoefficients(int numberOfDesieredDFTCoefficients) {
		this.numberOfDesieredDFTCoefficients = numberOfDesieredDFTCoefficients;
	}

	public void disableRekursiv() {
		this.rekursiv = false;
	}
	
	public void enableRekursiv() {
		this.rekursiv = true;
	}


	public SFA(double [] alphabet, int wordLength,boolean meanCorrected) {
		this.alphabet  = alphabet;
		
		//The wordlength must be even
		this.numberOfDesieredDFTCoefficients = wordLength/2;
	}
	
	@Override
	public TimeSeriesDataset transform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		
		if(input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		if(!fitted) {
			throw new NoneFittedFilterExeception("The filter must be fitted before it can transform.");
		}
		
		
		sfaDataset = new ArrayList<double[][]>();
		//calculate SFA words for every instance and its DFT coefficients 
		for(int matrix = 0; matrix < dFTDataset.getNumberOfVariables(); matrix++) {
			double[][] sfaWords = new double [(int) dFTDataset.getNumberOfInstances()][numberOfDesieredDFTCoefficients*2];
			for(int instance = 0; instance < dFTDataset.getNumberOfInstances(); instance++) {
				for(int entry = 0; entry < numberOfDesieredDFTCoefficients*2; entry++) {
					double elem = dFTDataset.getValues(matrix)[instance][entry];
					// get the lookup table for DFT values of the instance 
					double lookup [] = lookupTable.get(matrix)[entry];
					
					// if the DFT coefficient is smaller than the first or larger than the last
					// or it lays on the border give it first, last or second ,penultimate  
					if(elem < lookup[0]) {
						sfaWords[instance][entry] = alphabet[0];
					}else 
					if(elem == lookup[0]){
						sfaWords[instance][entry] = alphabet[1];
					}else
					if(elem > lookup[alphabet.length-2]) {
						sfaWords[instance][entry] = alphabet[alphabet.length-1];
					}else
					if(elem == lookup[alphabet.length-2]) {
						sfaWords[instance][entry] = alphabet[alphabet.length-1];
					}
					//get alphabet letter for every non extrem coefficient
					else {
						for(int i = 1; i < lookup.length; i++) {
							if(elem < lookup[i]) {
								sfaWords[instance][entry] = alphabet[i];
								break;
							}
							if(elem == lookup[i]){
								sfaWords[instance][entry] = alphabet[i+1];
								break;
							}
						} 
					}
				}
			}
				sfaDataset.add(sfaWords);	
			}
			TimeSeriesDataset output = new TimeSeriesDataset(sfaDataset, null, null);
			return output;
	}

	@Override
	public void fit(TimeSeriesDataset input) {
		// TODO Auto-generated method stub
	
		if(input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		
		if(alphabet.length == 0) {
			throw new IllegalArgumentException("The alphabet size can not be zero.");
		}
		
		lookupTable.clear();
		
		dftFilter = new DFT();
		dftFilter.setMeanCorrected(meanCorrected);
		
		try {
			//calculates the number of DFT coefficents with wordlength as number of desired DFT coefficients
			dftFilter.setNumberOfDisieredCoefficients(numberOfDesieredDFTCoefficients);
			
			if(!rekursiv) {
				dFTDataset = (TimeSeriesDataset) dftFilter.fitTransform(input);
			}
			else {
				// Only works for sliding windows. However it is normally used for SFA. 
				dFTDataset = (TimeSeriesDataset) dftFilter.rekursivDFT(input);
			}
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int matrix = 0; matrix < dFTDataset.getNumberOfVariables(); matrix++) {
			//for each part of every coefficient calculate the bins for the alphabet (number of bins == number of letters)
			double[][] lookUpTable = new double [numberOfDesieredDFTCoefficients*2][alphabet.length-1];
			
			for(int coeficient = 0; coeficient < numberOfDesieredDFTCoefficients*2; coeficient++) {
				//get the columns of the DFT dataset 
				double[] toBin = new double[(int)input.getNumberOfInstances()]; 
				for(int instances = 0; instances < dFTDataset.getNumberOfInstances(); instances++) {
					toBin[instances]= dFTDataset.getValues(matrix)[instances][coeficient];
				}
				
				//Sort ascending
				//If the number of instances is equal to the number of bins the breakpoints are set to this values
				Arrays.sort(toBin);
				if(toBin.length == alphabet.length-1) {
					for(int alphabetLetter = 0; alphabetLetter< alphabet.length-1;alphabetLetter++) {
						lookUpTable[coeficient][alphabetLetter] = toBin[alphabetLetter];
					}
				}
				//	If the number of instances is greater than the number of bins then the breakpoints are set
				//  in the way that all coefficients are spread equally over the bins
				else {
					int splitValue=(int) Math.round(toBin.length/alphabet.length);
					for(int alphabetLetter = 1; alphabetLetter < alphabet.length; alphabetLetter++) {
						lookUpTable[coeficient][alphabetLetter-1] = toBin[alphabetLetter*splitValue];
					}
				}
				
			}
			lookupTable.add(lookUpTable);
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
		
		throw new UnsupportedOperationException("To build a SFA word the full dataset has to be considerd therefore it is not reasonable in this context to perform this operation on a single Instance.");
	}


	@Override
	public void fit(double[] input) throws IllegalArgumentException {
		
		throw new UnsupportedOperationException("To build a SFA word the full dataset has to be considerd therefore it is not reasonable in this context to perform this operation on a single Instance.");
	}


	@Override
	public double[] fitTransform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		
		throw new UnsupportedOperationException("To build a SFA word the full dataset has to be considerd therefore it is not reasonable in this context to perform this operation on a single Instance.");
	}


	@Override
	public double[][] transform(double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		if(input.length == 0) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		if(!fittedMatrix) {
			throw new NoneFittedFilterExeception("The filter must be fitted before it can transform.");
		}
		
		sfaMatrix = new double [(int) dftMatrix.length][numberOfDesieredDFTCoefficients*2];
		for(int instance = 0; instance < dftMatrix.length; instance++) {
			for(int entry = 0; entry < numberOfDesieredDFTCoefficients*2; entry++) {
				double elem = dftMatrix[instance][entry];
				// get the lookup table for DFT values of the instance 
				double lookup [] = lookUpTableMatrix[entry];
				
				// if the DFT coefficient is smaller than the first or larger than the last
				// or it lays on the border give it first, last or second ,penultimate  
				if(elem < lookup[0]) {
					sfaMatrix[instance][entry] = alphabet[0];
				}
				if(elem == lookup[0]){
					sfaMatrix[instance][entry] = alphabet[1];
				}
				if(elem > lookup[alphabet.length-2]) {
					sfaMatrix[instance][entry] = alphabet[alphabet.length-1];
				}
				if(elem == lookup[alphabet.length-2]) {
					sfaMatrix[instance][entry] = alphabet[alphabet.length-1];
				}
				//get alphabet letter for every non extrem coefficient
				else {
					for(int i = 1; i < lookup.length-2; i++) {
						if(elem > lookup[i]) {
							sfaMatrix[instance][entry] = alphabet[i];
						}
						if(elem == lookup[i]){
							sfaMatrix[instance][entry] = alphabet[i+1];
						}
					} 
				}
			}
		}
		
		return sfaMatrix;
	}

	/*
	 * Can not be called in the fit dataset method because it needs its own DFT-
	 * Filter and for the dataset an overall Filter is needed.
	 */
	@Override
	public void fit(double[][] input) throws IllegalArgumentException {
		if(input.length == 0) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		
		if(alphabet.length == 0) {
			throw new IllegalArgumentException("The alphabet size can not be zero.");
		}
		
		
		dftFilterMatrix = new DFT();
		
		try {
			//calculates the number of DFT coefficents with wordlength as number of desired DFT coefficients
			dftFilterMatrix.setNumberOfDisieredCoefficients(numberOfDesieredDFTCoefficients);
			if(!rekursiv) {
			dftMatrix =  dftFilterMatrix.fitTransform(input);
			}
			else {
				dftMatrix = dftFilterMatrix.rekursivDFT(input);
			}
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		lookUpTableMatrix = new double [numberOfDesieredDFTCoefficients*2][alphabet.length-1];
		
		for(int coeficient = 0; coeficient < numberOfDesieredDFTCoefficients*2; coeficient++) {
			//get the columns of the DFT dataset 
			double[] toBin = new double[input.length]; 
			for(int instances = 0; instances < input.length; instances++) {
				toBin[instances]= dftMatrix[instances][coeficient];
			}
			
			//Sort ascending
			//If the number of instances is equal to the number of bins the breakpoints are set to this values
			Arrays.sort(toBin);
			if(toBin.length == alphabet.length-1) {
				for(int alphabetLetter = 0; alphabetLetter< alphabet.length-1;alphabetLetter++) {
					lookUpTableMatrix[coeficient][alphabetLetter] = toBin[alphabetLetter];
				}
			}
			//	If the number of instances is greater than the number of bins then the breakpoints are set
			//  in the way that all coefficients are spread equally over the bins
			else {
				int splitValue=(int) Math.round(toBin.length/alphabet.length);
				for(int alphabetLetter = 0; alphabetLetter < alphabet.length-1; alphabetLetter++) {
					lookUpTableMatrix[coeficient][alphabetLetter] = toBin[alphabetLetter+splitValue];
				}
			}
			
		}
		
		fittedMatrix = true;
		
	}


	@Override
	public double[][] fitTransform(double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

}
