package de.upb.crc901.mlplan.core;

import java.io.Serializable;

import jaicore.search.algorithms.standard.core.ICancelableNodeEvaluator;
import weka.classifiers.Classifier;
import weka.core.Instances;

public interface SolutionEvaluator extends Serializable, ICancelableNodeEvaluator {
	
	public void setData(Instances train);
	
	public Integer getSolutionScore(Classifier solution) throws Throwable;
}
