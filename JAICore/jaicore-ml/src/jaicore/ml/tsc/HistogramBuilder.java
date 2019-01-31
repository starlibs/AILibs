package jaicore.ml.tsc;

import java.util.Arrays;
import java.util.HashMap;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;

public class HistogramBuilder {
	private boolean fitted=false;
	private int windowLength;
	private int orignalLengthOfInstances;
	private HashMap<Integer,Integer>[][] histograms;
	private int originalNumberOfInstnaces;
	//private ArrayList<HashMap<Integer,Integer>> histograms = new ArrayList<HashMap<Integer,Integer>>();
	
	
	public void setOrignalLengthOfInstances(int orignalLengthOfInstances) {
		this.orignalLengthOfInstances = orignalLengthOfInstances;
	}

	 public void setWindowLength(int windowLength) { this.windowLength =
	  windowLength; }

	public void buildHistograms(TimeSeriesDataset input) {
		if(input.isEmpty()) {
			throw new IllegalArgumentException("The input dataset can not be empty");
		}
		
		if(new Double(windowLength).equals(null)) {
			throw new IllegalArgumentException("The windowlength must be set.");
		}
		
		if(new Double(orignalLengthOfInstances).equals(null)) {
			throw new IllegalArgumentException("The orignal instance length must be set.");
		}
		
		if(new Double(originalNumberOfInstnaces).equals(null)) {
			throw new IllegalArgumentException("The orignal number of instances must be set.");
		}
		
		histograms = (HashMap<Integer,Integer>[][]) new HashMap[input.getNumberOfVariables()][originalNumberOfInstnaces];
		
		for(int matrix = 0; matrix<input.getNumberOfVariables(); matrix++) {
			HashMap<Integer,Integer>[] matrixHistograms = (HashMap<Integer, Integer>[])new HashMap[originalNumberOfInstnaces];
			for(int instance = 0; instance < input.getNumberOfInstances(); instance+=(orignalLengthOfInstances-windowLength-1)) {
				HashMap<Integer,Integer> histogram = new HashMap<Integer,Integer>();
				for(int entry = instance; entry < (instance + (orignalLengthOfInstances-windowLength-1)); entry++) {
					if(histogram.containsKey(Arrays.hashCode(input.getValues(matrix)[entry]))) {
						histogram.replace(Arrays.hashCode(input.getValues(matrix)[entry]), histogram.get(Arrays.hashCode(input.getValues(matrix)[entry])+1));
					}
					else {
						histogram.put(Arrays.hashCode(input.getValues(matrix)[entry]), 1);
					}
				}
				matrixHistograms[instance]=histogram;
			}
			histograms[matrix] = matrixHistograms;
		}
	}

}
