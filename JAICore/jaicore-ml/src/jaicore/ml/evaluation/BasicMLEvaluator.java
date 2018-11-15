package jaicore.ml.evaluation;

import com.google.common.eventbus.EventBus;

import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Provides the mean error over all instances + the error of the 0.25-percentile
 * 
 * @author fmohr
 *
 */
public interface BasicMLEvaluator {
	void setReproducibilityEventBus(EventBus e);
	
	public double getErrorRateForRandomSplit(Classifier c, Instances data, double splitSize) throws Exception;	
	public double getErrorRateForSplit(Classifier c, Instances train, Instances test) throws Exception;	
}
