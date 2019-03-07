package jaicore.ml.tsc.filter;


import java.util.Arrays;

import jaicore.ml.tsc.PPA;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class SAX implements IFilter {
	
	private double [] alphabet;
	private boolean fitted;
	private int wordLength;
	private double [][] lookuptable;
	private ZTransformer ztransform;
	private double [][] maxAndMin;
	
	public SAX(double[] alphabet, int wordLength) {
		this.ztransform = new ZTransformer();
		this.alphabet  = alphabet;
		this.wordLength = wordLength;
	}

	@Override
	public TimeSeriesDataset transform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
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
			double [][] newMatrix = new double[(int) input.getNumberOfInstances()][wordLength]; 
			
			for(int instance = 0; instance < input.getNumberOfInstances(); instance++) {
				double[] PPAOfInstance = PPA.ppa(input.getValues(matrix)[instance], wordLength);
				double[] TSasString = new double[wordLength];
				double [] localLookupTable = lookuptable[matrix];
				
				for(int i = 0; i < (int)PPAOfInstance.length;i++) {
					double ppaValue = PPAOfInstance[i];
					boolean valuefound = false;
					for(int j = 0; j < localLookupTable.length; j++) {
						if(ppaValue<localLookupTable[j]) {
							TSasString[i] = alphabet[j];
							valuefound = true;
						}
					}
					
					//TODO testen !!! evt put data into tree 
					if(valuefound == false) {
						TSasString[i] =alphabet[alphabet.length-1];
					}
				}
				
				newMatrix[instance] = TSasString;
			}
			
			sAXTransformedDataset.add(newMatrix, null);
		}
		
		
		return sAXTransformedDataset;
	}

	@Override
	public void fit(TimeSeriesDataset input) {
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timeseriesdatasets");
		}
		if(((TimeSeriesDataset)input).isEmpty()){
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		// TODO Can a ppa value be smaller than the smallest value or higher then the highest
		maxAndMin = new double [2][input.getNumberOfVariables()];
		try {
			ztransform.fitTransform(input);
			for(int matrix = 0; matrix < input.getNumberOfVariables(); matrix++){
				
				double[] max = new double [(int)input.getNumberOfInstances()]; 
				double[] min = new double [(int)input.getNumberOfInstances()];
				for(int instance = 0; instance < input.getNumberOfInstances(); instance++) {
					max[instance] = Arrays.stream(input.getValues(matrix)[instance]).max().getAsDouble();
					min[instance] = Arrays.stream(input.getValues(matrix)[instance]).min().getAsDouble();
				}
					maxAndMin[0][matrix] = Arrays.stream(max).max().getAsDouble();
					maxAndMin[1][matrix] = Arrays.stream(min).min().getAsDouble();
			}
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//filling the lookuptable
		this.lookuptable = new double [input.getNumberOfVariables()][alphabet.length];
		
		for(int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {
			double [] localMaxMin =  new double[] {maxAndMin[0][matrix], maxAndMin[1][matrix]};
			double totalsize = localMaxMin[0]-localMaxMin[1];
			double stepsize = totalsize/alphabet.length;
			
			lookuptable[matrix][0] = localMaxMin[1]+stepsize;
			for(int i = 1; i < alphabet.length;i++) {
				lookuptable[matrix][i] = lookuptable[matrix][i-1]+stepsize;
			}
		}
		fitted = true;
	}

	@Override
	public TimeSeriesDataset fitTransform(TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		fit(input);
		return transform(input);
	}

	@Override
	public double[] transform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fit(double[] input) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[] fitTransform(double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[][] transform(double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fit(double[][] input) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[][] fitTransform(double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		return null;
	}

}
