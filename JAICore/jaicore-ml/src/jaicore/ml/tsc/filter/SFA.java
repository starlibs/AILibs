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

	
	private ArrayList<double[][]> lookupTable = null;
	private ArrayList<double[][]> sfaDataset = null;
	
	public void setNumberOfDesieredDFTCoefficients(int numberOfDesieredDFTCoefficients) {
		this.numberOfDesieredDFTCoefficients = numberOfDesieredDFTCoefficients;
	}


	public SFA(double [] alphabet, int wordLength) {
		this.alphabet  = alphabet;
		this.numberOfDesieredDFTCoefficients = wordLength;
	}
	
	@Override
	public TimeSeriesDataset transform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}
		if(((TimeSeriesDataset)input).isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		if(!fitted) {
			throw new NoneFittedFilterExeception("The filter must be fitted before it can transform.");
		}
		//TODO Sliding window is still missing 
		
		sfaDataset = new ArrayList<double[][]>();
		
		for(int matrix = 0; matrix < DFTDataset.getNumberOfVariables(); matrix++) {
			double[][] sfaWords = new double [(int) DFTDataset.getNumberOfInstances()][numberOfDesieredDFTCoefficients];
			for(int instance = 0; instance < DFTDataset.getNumberOfInstances(); instance++) {
				for(int entry = 0; entry < numberOfDesieredDFTCoefficients; entry++) {
					double elem = DFTDataset.getValues(matrix)[instance][entry];
					double lookup [] = lookupTable.get(matrix)[instance];
					for(int i = 0; i < lookup.length; i=+2) {
						if(elem > lookup[i]& elem < lookup[i+1]) {
							sfaWords[instance][entry] = alphabet[i/2];
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
	
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}
		if(((TimeSeriesDataset)input).isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		
		if(alphabet.length == 0) {
			throw new IllegalArgumentException("The alphabet size can not be zero.");
		}
		
		
		DFT dftFilter = new DFT();
		
		try {
			dftFilter.setNumberOfDisieredCoefficients(numberOfDesieredDFTCoefficients);
			DFTDataset = (TimeSeriesDataset) dftFilter.fitTransform(input);
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<double[][]> lookupTable = new ArrayList<double[][]>(); 
		
		
		for(int matrix = 0; matrix < DFTDataset.getNumberOfVariables(); matrix++) {
			int index = 0;
			double[][] lookUpTable = new double [numberOfDesieredDFTCoefficients*2][alphabet.length*2];
			for(int coeficient = 0; coeficient < numberOfDesieredDFTCoefficients*2; coeficient++) {
				
				double[] toBin = new double[(int)input.getNumberOfInstances()]; 
				
				for(int instances = 0; instances < DFTDataset.getNumberOfInstances(); instances++) {
					toBin[instances]= DFTDataset.getValues(matrix)[instances][coeficient];
				}
				//Sort ascending
				Arrays.sort(toBin);
				long splitValue = toBin.length/alphabet.length;
				//TODO TEST!!
				for(int alphabetLetter = 0; alphabetLetter < alphabet.length*2; alphabetLetter+=2) {
					index += (alphabetLetter/2)*splitValue;
					lookUpTable[coeficient][alphabetLetter] = toBin[index];
					lookUpTable[coeficient][alphabetLetter+1] = toBin[(int) (index+splitValue-1)];
				}
			}
			lookupTable.add(lookUpTable);
		}
		
		fitted = true;
	}

	@Override
	public TimeSeriesDataset fitTransform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stump
		fit(input);
		return transform(input);
	}

}
