package ai.libs.jaicore.ml.classification.singlelabel.timeseries.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;

/**
 * @author Helen Beierling
 *         This class is used to compute Histograms for the found sfa words.
 *         This includes a numerosity reduction.
 *         (in form of double sequences which are used as key by using the Arrays class HashCode which are Integer).
 *         c.f. p. 1514 "The BOSS is concerned with time series classification in the presence of noise" by Patrick Schaefer
 */
public class HistogramBuilder {
	private Map<Integer, Integer> histogram = new HashMap<>();

	public Map<Integer, Integer> histogramForInstance(final TimeSeriesDataset2 blownUpSingleInstance) {
		this.histogram.clear();
		double[] lastWord = null;

		// The blown up instance contains only one matrix.
		for (double[] d : blownUpSingleInstance.getValues(0)) {
			if (this.histogram.containsKey(Arrays.hashCode(d))) {
				/*
				 * To the histogramm suczessiv duplicates are not added because of numerosity reduction.
				 * c.f.p.1514
				 * "The BOSS is concerned with time series classification in the presence of noise by Patrick Schaefer"
				 */
				if (!Arrays.equals(d, lastWord)) {
					this.histogram.replace(Arrays.hashCode(d), this.histogram.get(Arrays.hashCode(d)) + 1);
				}
			} else {
				this.histogram.put(Arrays.hashCode(d), 1);
			}
			lastWord = d;
		}
		return this.histogram;
	}

}
