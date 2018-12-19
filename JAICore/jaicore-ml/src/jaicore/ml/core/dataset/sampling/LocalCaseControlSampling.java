package jaicore.ml.core.dataset.sampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.sets.SetUtil.Pair;
import weka.classifiers.Classifier;
import jaicore.ml.core.dataset.*;

public class LocalCaseControlSampling extends PilotEstimateSampling { 
	
	public LocalCaseControlSampling(Random rand, int preSampleSize, Classifier pilotEstimateClassifier) {
		this.rand = rand;
		this.preSampleSize = preSampleSize;
		this.pilotEstimator = pilotEstimateClassifier;
	}
	
	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch(this.getState()) {
		case created:
			this.sample = null;
			//this.pilotEstimator.buildClassifier(this.getInput()); TODO
			
			HashMap<Object, Integer> classOccurrences = countClassOccurrences(this.getInput());
			
			// Count number of classes
			int numberOfClasses = classOccurrences.keySet().size();
						
			// Calculate Boundaries that define which Instances is choose for which random number
			probabilityBoundaries = calculateInstanceBoundaries(classOccurrences, numberOfClasses);
			
			double r;
			IInstance choosenInstance;
			for(int i = 0; i < this.preSampleSize; i++) {
				r = this.rand.nextDouble();
				choosenInstance = null;
				for(int j = 0; j < probabilityBoundaries.size(); j++) {
					if(probabilityBoundaries.get(j).getY().doubleValue() > r) {
						choosenInstance = probabilityBoundaries.get(j).getX();
					}
				}
				if(choosenInstance == null) {
					choosenInstance = probabilityBoundaries.get(probabilityBoundaries.size() - 1).getX();
				}
				this.sample.add(choosenInstance);
			}
			
			this.setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		default: 
			throw new IllegalStateException("Unknown algorithm state "+ this.getState());	
		}
	}
}
