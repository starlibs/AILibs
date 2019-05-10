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
	 * @param predictions
	 * @return
	 */
	public static int countMinMaxWrongOrder(List<double[]> predictions) {
		int numWrongOrder = 0;
		
		for (double[] prediction : predictions) {
			if (prediction[0] > prediction[1]) {
				numWrongOrder++;
			}
		}
		
		return numWrongOrder;	
	}
	
	public static double[] adjustedPredictionsEvaluation(List<double[]> predictions, Instances dataTestMin, Instances dataTestMax) {
		int n = predictions.size();
		double abs_error_min = 0;
		double abs_error_max = 0;
		
		for (int i = 0; i < n; i++) {
			double min_pred = (predictions.get(i)[0] < predictions.get(i)[1]) ? predictions.get(i)[0] : predictions.get(i)[1];
			double max_pred = (predictions.get(i)[0] > predictions.get(i)[1]) ? predictions.get(i)[0] : predictions.get(i)[1];
			
			abs_error_min += Math.abs(min_pred - dataTestMin.get(i).classValue());
			abs_error_max += Math.abs(max_pred - dataTestMax.get(i).classValue());
		}
		double[] avg_abs_errors = new double[2];
		avg_abs_errors[0] = abs_error_min / n;
		avg_abs_errors[1] = abs_error_max / n;
		return avg_abs_errors;
	}
	
	/**
	 * Generates range query predictions for given test data and trained classifiers for minimum and maximum respectively.
	 * @param c_min
	 * @param c_max
	 * @param dataTestMin
	 * @param dataTestMax
	 * @return
	 */
	public static List<double[]> generateMinMaxPredictions(Classifier c_min, Classifier c_max, Instances dataTestMin, Instances dataTestMax) {
		int n = dataTestMin.size();
		List <double[]> predictions = new ArrayList<double[]>(n);
		
		for (int i = 0; i < n; i++) {
			double[] prediction = new double[2];
			try {
				prediction[0] = c_min.classifyInstance(dataTestMin.get(i));
				prediction[1] = c_max.classifyInstance(dataTestMax.get(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
			predictions.add(prediction);			
		}
		
		return predictions;
	}

}
