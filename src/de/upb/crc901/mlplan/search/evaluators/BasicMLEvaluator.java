package de.upb.crc901.mlplan.search.evaluators;

import weka.classifiers.Classifier;
import weka.core.Instances;

public interface BasicMLEvaluator {
	public double getErrorRateForRandomSplit(Classifier c, Instances data, double splitSize) throws Exception;	
	public double getErrorRateForSplit(Classifier c, Instances train, Instances test) throws Exception;	
}
