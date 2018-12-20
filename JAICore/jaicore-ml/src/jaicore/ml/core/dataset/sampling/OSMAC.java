package jaicore.ml.core.dataset.sampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class OSMAC extends PilotEstimateSampling {
	
	public OSMAC(Random rand, int preSampleSize) {
		this.rand = rand;
		this.preSampleSize = preSampleSize;
	}
	
	protected ArrayList<Pair<IInstance, Double>> calculateFinalInstanceBoundaries(IDataset<IInstance> instances, Classifier pilotEstimator) throws Exception {
		double boundaryOfCurrentInstance = 0.0;
		probabilityBoundaries = new ArrayList<Pair<IInstance, Double>>();
		int notSet = 0;
		double sumOfDistributionLosses = 0;
		int sumOfVectorLengths = 0;
		int vectorLength;
		for(IInstance instance: instances) {
			for(double dimensionLength: instance.getAsDoubleVector()) {
				sumOfVectorLengths += dimensionLength;
			}
			sumOfDistributionLosses += (1 - pilotEstimator.distributionForInstance((Instance) instance)[notSet]) //TODO need Class
					* sumOfVectorLengths;
		}
		for(IInstance instance: instances) {
			vectorLength = 0;
			for(double dimensionLength: instance.getAsDoubleVector()) {
				vectorLength += dimensionLength;
			}
			boundaryOfCurrentInstance += (1 - pilotEstimator.distributionForInstance((Instance) instance)[notSet]) //TODO need Class
					 * vectorLength / sumOfDistributionLosses;
			probabilityBoundaries.add(new Pair<IInstance, Double>((IInstance) instance, new Double(boundaryOfCurrentInstance)));
		}
		return null;
	}
}
