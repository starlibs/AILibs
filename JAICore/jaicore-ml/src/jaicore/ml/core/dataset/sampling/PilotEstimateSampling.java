package jaicore.ml.core.dataset.sampling;

import weka.classifiers.Classifier;

public abstract class PilotEstimateSampling extends CaseControlLikeSampling {
	
	protected int preSampleSize;
	protected Classifier pilotEstimator;

}
