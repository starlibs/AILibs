package jaicore.ml.core.dataset.sampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.standard.SimpleDataset;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Instance;

public abstract class PilotEstimateSampling <I extends IInstance> extends CaseControlLikeSampling<I> {
	
	protected int preSampleSize;
	protected Classifier pilotEstimator = new Logistic();
	
	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		ArrayList<Pair<I, Double>> probabilityBoundaries = null;
		switch(this.getState()) {
		case created:
			this.sample = this.getInput().createEmpty();
			IDataset<I> pilotEstimateSample = this.getInput().createEmpty();
			
			HashMap<Object, Integer> classOccurrences = countClassOccurrences(this.getInput());
			
			// Count number of classes
			int numberOfClasses = classOccurrences.keySet().size();
						
			// Calculate Boundaries that define which Instances is choose for which random number
			probabilityBoundaries = calculateInstanceBoundaries(classOccurrences, numberOfClasses);
			
			double r;
			I choosenInstance;
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
				pilotEstimateSample.add(choosenInstance);
			}
			Instances pilotEstimateInstances = WekaInstancesUtil.datasetToWekaInstances(pilotEstimateSample);
			this.pilotEstimator.buildClassifier(pilotEstimateInstances);
			
			probabilityBoundaries = calculateFinalInstanceBoundaries(pilotEstimateInstances, this.pilotEstimator);
			
			this.setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		case active:
			if(this.sample.size() < this.sampleSize) {
				choosenInstance = null;
				do {
					r = this.rand.nextDouble();
					for(int i = 0; i < probabilityBoundaries.size(); i++) {
						if(probabilityBoundaries.get(i).getY().doubleValue() > r) {
							choosenInstance = probabilityBoundaries.get(i).getX();
						}
					}
					if(choosenInstance == null) {
						choosenInstance = probabilityBoundaries.get(probabilityBoundaries.size() - 1).getX();
					}
				} while(this.sample.contains(choosenInstance));
				this.sample.add(choosenInstance);
				return new SampleElementAddedEvent();
			}
			else {
				this.setState(AlgorithmState.inactive);
				return new AlgorithmFinishedEvent();
			}
		case inactive:
			if (this.sample.size() < this.sampleSize) {
				throw new RuntimeException("Expected sample size was not reached before termination");
			} else {
				return new AlgorithmFinishedEvent();
			}
		default: 
			throw new IllegalStateException("Unknown algorithm state "+ this.getState());	
		}
	}
	
	abstract ArrayList<Pair<I, Double>> calculateFinalInstanceBoundaries(Instances instances, Classifier pilotEstimator) throws Exception;
}
