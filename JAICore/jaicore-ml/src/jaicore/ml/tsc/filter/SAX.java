package jaicore.ml.tsc.filter;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.PPA;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class SAX implements IFilter {
	
	private INDArray alphabet;
	private boolean fitted;
	private int wordLength;
	private INDArray lookuptable;
	private TimeSeriesDataset zTransformedDataset;
	private ZTransformer ztransform;
	private INDArray maxAndMin;
	
	public SAX(INDArray alphabet, int wordLength) {
		this.ztransform = new ZTransformer();
		this.alphabet  = alphabet;
		this.wordLength = wordLength;
	}

	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		
		if(!(input instanceof TimeSeriesDataset)){
			throw new IllegalArgumentException("This method only supports TimeSeriesDatasets");
		}
		if(((TimeSeriesDataset) input).isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}	
		if(!fitted) {
			throw new NoneFittedFilterExeception("Fit() must be called before transform()");
		}
		
		TimeSeriesDataset sAXTransformedDataset = new TimeSeriesDataset(null, null, null);
		
		for(int matrix = 0; matrix < ((TimeSeriesDataset) input).getNumberOfVariables(); matrix++) {
			INDArray newMatrix = Nd4j.valueArrayOf(new long[] {((TimeSeriesDataset) input).getNumberOfInstances(), wordLength},0); 
			
			for(int instance = 0; instance < ((TimeSeriesDataset) input).getNumberOfInstances(); instance++) {
				INDArray INDArrayPPAOfInstance = PPA.ppa(((TimeSeriesDataset) input).getValues(matrix).getRow(instance), wordLength);
				INDArray TSasString = Nd4j.zeros(wordLength);
				INDArray localLookupTable = lookuptable.getRow(matrix);
				
				for(int i = 0; i < (int)INDArrayPPAOfInstance.length();i++) {
					double ppaValue = INDArrayPPAOfInstance.getDouble(i);
					boolean valuefound = false;
					for(int j = 0; j < localLookupTable.length(); j++) {
						if(ppaValue<localLookupTable.getDouble(j)) {
							TSasString.putScalar(i, alphabet.getDouble(j));
							valuefound = true;
						}
					}
					
					//TODO testen !!! evt put data into tree 
					if(valuefound == false) {
						TSasString.putScalar(i, alphabet.getDouble(alphabet.length()-1));
					}
				}
				
				newMatrix.putRow(instance, TSasString);
			}
			
			sAXTransformedDataset.add(newMatrix, null);
		}
		
		
		return sAXTransformedDataset;
	}

	@Override
	public void fit(IDataset input) {
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timeseriesdatasets");
		}
		if(((TimeSeriesDataset)input).isEmpty()){
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		// TODO Can a ppa value be smaller than the smallest value or higher then the highest
		maxAndMin = Nd4j.valueArrayOf(new int []{2,((TimeSeriesDataset) input).getNumberOfVariables()}, 0);
		try {
			zTransformedDataset = (TimeSeriesDataset)ztransform.fitTransform(input);
			for(int matrix = 0; matrix < ((TimeSeriesDataset) input).getNumberOfVariables(); matrix++){
					maxAndMin.put(new int [] {0,matrix}, ((TimeSeriesDataset) input).getValues(matrix).max(1).max(1));
					maxAndMin.put(new int [] {1,matrix}, ((TimeSeriesDataset) input).getValues(matrix).min(1).min(1));		
			}
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//filling the lookuptable
		this.lookuptable = Nd4j.valueArrayOf(new int[] {((TimeSeriesDataset) input).getNumberOfVariables(),(int) alphabet.length()},0);
		
		for(int matrix = 0; matrix < ((TimeSeriesDataset) input).getNumberOfVariables(); matrix++) {
			INDArray localMaxMin =  maxAndMin.getColumn(matrix);
			double totalsize = localMaxMin.getDouble(0)-localMaxMin.getDouble(1);
			double stepsize = totalsize/alphabet.length();
			
			lookuptable.putScalar(new int[] {matrix,0}, localMaxMin.getDouble(1)+stepsize);
			for(int i = 1; i < lookuptable.columns();i++) {
				lookuptable.putScalar(new int[] {matrix,i},lookuptable.getDouble(i-1)+stepsize);
			}
		}
		fitted = true;
	}

	@Override
	public IDataset fitTransform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		fit(input);
		return transform(input);
	}

}
