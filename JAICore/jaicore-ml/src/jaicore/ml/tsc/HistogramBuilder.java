package jaicore.ml.tsc;

import java.util.Arrays;
import java.util.HashMap;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;

/**
 * @author Helen Beierling
 * This class is used to compute Histograms for the found sfa words.
 * This includes a numerosity reduction.
 * (in form of double sequences which are used as key by using the Arrays class HashCode which are Integer).
 * c.f. p. 1514 "The BOSS is concerned with time series classification in the presence of noise" by Patrick Schäfer
 */
public class HistogramBuilder {
	private HashMap<Integer,Integer> histogram = new HashMap<Integer,Integer>();
	
	public HashMap<Integer,Integer> histogramForInstance(TimeSeriesDataset blownUpSingleInstance){
		histogram.clear();
		double [] lastWord = null;
		
		//The blown up instance contains only one matrix.
		for(double [] d : blownUpSingleInstance.getValues(0)) {		
				if(histogram.containsKey(Arrays.hashCode(d))) {
				/*
				 * To the histogramm suczessiv duplicates are not added because of numerosity reduction.
				 * c.f.p.1514
				 * "The BOSS is concerned with time series classification in the presence of noise by Patrick Schäfer"
				 */
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
