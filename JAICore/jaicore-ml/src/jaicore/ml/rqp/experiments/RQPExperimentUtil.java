package jaicore.ml.rqp.experiments;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class RQPExperimentUtil {
	
	/**
	 * Counts how many times a pair of classifiers for predictions of minima and maxima in a range query cause an invalid prediction,
	 * i.e. how many times do they predict min > max for a given data set?
	 * @param c_min
	 * @param c_max
	 * @param dataTestMin
	 * @param dataTestMax
	 * @return
	 */
	public static int countMinMaxWrongOrder(Classifier c_min, Classifier c_max, Instances dataTestMin, Instances dataTestMax) {
		int n = dataTestMin.size();
		int numWrongOrder = 0;
		
		for (int i = 0; i < n; i++) {
			try {
				double minPrediction = c_min.classifyInstance(dataTestMin.get(i));
				double maxPrediction = c_max.classifyInstance(dataTestMax.get(i));
				if (minPrediction > maxPrediction) {
					numWrongOrder++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return numWrongOrder;	
	}

}
