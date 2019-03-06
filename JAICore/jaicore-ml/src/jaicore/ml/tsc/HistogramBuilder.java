package jaicore.ml.tsc;

import java.util.Arrays;
import java.util.HashMap;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;

public class HistogramBuilder {
	private HashMap<Integer,Integer> histogram = new HashMap<Integer,Integer>();
	
	public HashMap<Integer,Integer> histogramForInstance(TimeSeriesDataset blownUpSingleInstance){
		double [] lastWord = null;
		for(double [] d : blownUpSingleInstance.getValues(0)) {
				if(histogram.containsKey(Arrays.hashCode(d))) {
					if(!Arrays.equals(d, lastWord)) {
					histogram.replace(Arrays.hashCode(d),histogram.get(Arrays.hashCode(d))+1);
					}
				}
				else {
					histogram.put(Arrays.hashCode(d), 1);
				}
				lastWord = d;
			}
		return histogram;
	}
	
}
