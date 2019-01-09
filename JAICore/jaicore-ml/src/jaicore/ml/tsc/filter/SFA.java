package jaicore.ml.tsc.filter;

import java.util.ArrayList;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class SFA implements IFilter {

	private INDArray alphabet;
	
	
	private boolean fitted = false;
	
	private TimeSeriesDataset DFTDataset = null;
	private int numberOfDesieredDFTCoefficients;

	
	private ArrayList<INDArray> lookupTable = null;
	private ArrayList<INDArray> sfaDataset = null;
	
	public void setNumberOfDesieredDFTCoefficients(int numberOfDesieredDFTCoefficients) {
		this.numberOfDesieredDFTCoefficients = numberOfDesieredDFTCoefficients;
	}

	//TODO should I use long ?
	public SFA(INDArray alphabet, int wordLength) {
		this.alphabet  = alphabet;
		this.numberOfDesieredDFTCoefficients = wordLength;
	}
	
	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		//TODO is empty ??
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}
		
		if(!fitted) {
			throw new NoneFittedFilterExeception("The filter must be fitted before it can transform.");
		}
		//TODO Sliding window is still missing 
		
		sfaDataset = new ArrayList<INDArray>();
		
		for(int matrix = 0; matrix < DFTDataset.getNumberOfVariables(); matrix++) {
			INDArray sfaWords = Nd4j.valueArrayOf(new long [] {DFTDataset.getNumberOfInstances(),numberOfDesieredDFTCoefficients},0);
			for(int instance = 0; instance < DFTDataset.getNumberOfInstances(); instance++) {
				for(int entry = 0; entry < numberOfDesieredDFTCoefficients; entry++) {
					double elem = DFTDataset.getValues(matrix).getRow(instance).getDouble(entry);
					INDArray lookup = lookupTable.get(matrix).getRow(instance);
					for(int i = 0; i < lookup.length(); i=+2) {
						if(elem > lookup.getDouble(i)& elem < lookup.getDouble(i+1)) {
							sfaWords.putScalar(new long[]{instance,entry}, alphabet.getDouble(i/2));
						}
					} 
				}
			}
				sfaDataset.add(sfaWords);
			}
			//TODO geht der Cast
			return (IDataset) sfaDataset;
	}

	@Override
	public void fit(IDataset input) {
		// TODO Auto-generated method stub
	
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}
		if(alphabet.length() == 0) {
			throw new IllegalArgumentException("The alphabet size can not be zero.");
		}
		if(alphabet.isEmpty()) {
			throw new IllegalArgumentException("The alphabet can not be null for this method.");
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
		ArrayList<INDArray> lookupTable = new ArrayList<INDArray>(); 
		
		
		for(int matrix = 0; matrix < DFTDataset.getNumberOfVariables(); matrix++) {
			int index = 0;
			INDArray lookUpTable = Nd4j.valueArrayOf(new int[]{numberOfDesieredDFTCoefficients*2,(int)alphabet.length()*2},0);
			for(int coeficient = 0; coeficient < numberOfDesieredDFTCoefficients*2; coeficient++) {
				
				INDArray toBin = Nd4j.valueArrayOf(new int[] {(int)((TimeSeriesDataset)input).getNumberOfInstances()},0); 
				
				for(int instances = 0; instances < DFTDataset.getNumberOfInstances(); instances++) {
					toBin.putScalar(instances,DFTDataset.getValues(instances).getDouble(coeficient));
				}
				//Sort ascending
				Nd4j.sort(toBin, true);
				long splitValue = toBin.length()/alphabet.length();
				//TODO TEST!!
				for(int alphabetLetter = 0; alphabetLetter < alphabet.length()*2; alphabetLetter+=2) {
					index += (alphabetLetter/2)*splitValue;
					lookUpTable.putScalar(new int[] {coeficient,alphabetLetter},toBin.getDouble(index));
					lookUpTable.putScalar(new int[] {coeficient,alphabetLetter+1},toBin.getDouble(index+splitValue-1));
				}
			}
			lookupTable.add(lookUpTable);
		}
		
		fitted = true;
	}

	@Override
	public IDataset fitTransform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports timeseries datasets.");
		}
		fit(input);
		
		return transform(input);
	}

}
