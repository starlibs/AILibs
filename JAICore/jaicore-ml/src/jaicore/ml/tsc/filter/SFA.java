package jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.Arrays;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class SFA implements IFilter {

	private double[] alphabet;
	
	
	private boolean fitted = false;
	
	private TimeSeriesDataset DFTDataset = null;
	private int numberOfDesieredDFTCoefficients;

	
	private ArrayList<double[][]> lookupTable = new ArrayList<double[][]>();
	private ArrayList<double[][]> sfaDataset = new ArrayList<double[][]>();
	
	public void setNumberOfDesieredDFTCoefficients(int numberOfDesieredDFTCoefficients) {
		this.numberOfDesieredDFTCoefficients = numberOfDesieredDFTCoefficients;
	}


	public SFA(double [] alphabet, int wordLength) {
		this.alphabet  = alphabet;
		//TODO wordlength /2
		this.numberOfDesieredDFTCoefficients = wordLength;
	}
	
	@Override
	public TimeSeriesDataset transform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		
		if(input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		if(!fitted) {
			throw new NoneFittedFilterExeception("The filter must be fitted before it can transform.");
		}
		//TODO Sliding window is still missing 
		
		sfaDataset = new ArrayList<double[][]>();
		//calculate SFA words for every instance and its DFT coefficients 
		for(int matrix = 0; matrix < DFTDataset.getNumberOfVariables(); matrix++) {
			double[][] sfaWords = new double [(int) DFTDataset.getNumberOfInstances()][numberOfDesieredDFTCoefficients*2];
			for(int instance = 0; instance < DFTDataset.getNumberOfInstances(); instance++) {
				for(int entry = 0; entry < numberOfDesieredDFTCoefficients*2; entry++) {
					double elem = DFTDataset.getValues(matrix)[instance][entry];
					// get the lookup table for DFT values of the instance 
					double lookup [] = lookupTable.get(matrix)[entry];
					
					// if the DFT coefficient is smaller than the first or larger than the last
					// or it lays on the border give it first, last or second ,penultimate  
					if(elem < lookup[0]) {
						sfaWords[instance][entry] = alphabet[0];
					}
					if(elem == lookup[0]){
						sfaWords[instance][entry] = alphabet[1];
					}
					if(elem > lookup[alphabet.length-2]) {
						sfaWords[instance][entry] = alphabet[alphabet.length-1];
					}
					if(elem == lookup[alphabet.length-2]) {
						sfaWords[instance][entry] = alphabet[alphabet.length-1];
					}
					//get alphabet letter for every non extrem coefficient
					else {
						for(int i = 1; i < lookup.length-2; i++) {
							if(elem > lookup[i]) {
								sfaWords[instance][entry] = alphabet[i];
							}
							if(elem == lookup[i]){
								sfaWords[instance][entry] = alphabet[i+1];
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
		
		
		DFT dftFilter = new DFT();
		
		try {
			//calculates the number of DFT coefficents with wordlength as number of desired DFT coefficients
			dftFilter.setNumberOfDisieredCoefficients(numberOfDesieredDFTCoefficients);
			DFTDataset = (TimeSeriesDataset) dftFilter.fitTransform(input);
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		for(int matrix = 0; matrix < DFTDataset.getNumberOfVariables(); matrix++) {
			//for each part of every coefficient claculate the bins for the alphabet (number of bins == number of letters)
			double[][] lookUpTable = new double [numberOfDesieredDFTCoefficients*2][alphabet.length-1];
			
			for(int coeficient = 0; coeficient < numberOfDesieredDFTCoefficients*2; coeficient++) {
				//get the columns of the DFT dataset 
				double[] toBin = new double[(int)input.getNumberOfInstances()]; 
				for(int instances = 0; instances < DFTDataset.getNumberOfInstances(); instances++) {
					toBin[instances]= DFTDataset.getValues(matrix)[instances][coeficient];
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
					for(int alphabetLetter = 0; alphabetLetter < alphabet.length-1; alphabetLetter++) {
						lookUpTable[coeficient][alphabetLetter] = toBin[alphabetLetter+splitValue];
					}
				}
				
			}
			lookupTable.add(lookUpTable);
		}
		
		fitted = true;
	}

	@Override
	public TimeSeriesDataset fitTransform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		
		throw new UnsupportedOperationException("To build a SFA word the full dataset has to be considerd therefore it is not reasonable in this context to perform this operation on a single Instance.");
	}


	@Override
	public double[] transformInstance(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		
		throw new UnsupportedOperationException("To build a SFA word the full dataset has to be considerd therefore it is not reasonable in this context to perform this operation on a single Instance.");
	}


	@Override
	public void fitInstance(double[] input) throws IllegalArgumentException {
		
		throw new UnsupportedOperationException("To build a SFA word the full dataset has to be considerd therefore it is not reasonable in this context to perform this operation on a single Instance.");
	}


	@Override
	public double[] fitTransformInstance(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		
		throw new UnsupportedOperationException("To build a SFA word the full dataset has to be considerd therefore it is not reasonable in this context to perform this operation on a single Instance.");
	}

}
