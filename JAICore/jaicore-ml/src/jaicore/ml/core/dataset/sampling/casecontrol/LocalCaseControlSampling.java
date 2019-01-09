package jaicore.ml.core.dataset.sampling.casecontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.sets.SetUtil.Pair;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import jaicore.ml.core.dataset.*;
import jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;
import jaicore.ml.core.dataset.sampling.WekaInstancesUtil;
import jaicore.ml.core.dataset.standard.SimpleInstance;

public class LocalCaseControlSampling <I extends IInstance> extends PilotEstimateSampling <I> { 
	
	public LocalCaseControlSampling(Random rand, int preSampleSize) {
		this.rand = rand;
		this.preSampleSize = preSampleSize;
	}
	
	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		ArrayList<Pair<I, Double>> probabilityBoundaries = null;
		switch(this.getState()) {
		case created:
			this.sample = this.getInput().createEmpty();
			IDataset<I> pilotEstimateSample = this.getInput().createEmpty();
			//this.pilotEstimator.buildClassifier(input);
			
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
			//Instances pilotEstimatorSample = new Instances((Instances) pilotEstimateSample);
			Instances pilotEstimatorSample = null; //TODO 
			this.pilotEstimator.buildClassifier(pilotEstimatorSample);
			
			probabilityBoundaries = calculateFinalInstanceBoundaries(WekaInstancesUtil.datasetToWekaInstances(pilotEstimateSample),
					this.pilotEstimator);
			
			this.setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		case active:
			if(this.sample.size() < this.sampleSize) {
				r = this.rand.nextDouble();
				choosenInstance = null;
				for(int i = 0; i < probabilityBoundaries.size(); i++) {
					if(probabilityBoundaries.get(i).getY().doubleValue() > r) {
						choosenInstance = probabilityBoundaries.get(i).getX();
					}
				}
				if(choosenInstance == null) {
					choosenInstance = probabilityBoundaries.get(probabilityBoundaries.size() - 1).getX();
				}
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
	
	protected ArrayList<Pair<I, Double>> calculateFinalInstanceBoundaries(Instances instances, Classifier pilotEstimator) throws Exception {
		double boundaryOfCurrentInstance = 0.0;
		ArrayList<Pair<Instance, Double>> instanceProbabilityBoundaries = new ArrayList<Pair<Instance, Double>>();
		double sumOfDistributionLosses = 0;
		for(Instance instance: instances) {
			sumOfDistributionLosses += 1 - pilotEstimator.distributionForInstance(instance)[instance.classIndex()];
		}
		for(Instance instance: instances) {
			boundaryOfCurrentInstance += 1 - pilotEstimator.distributionForInstance((Instance) instance)[instance.classIndex()]
					 / sumOfDistributionLosses;
			instanceProbabilityBoundaries.add(new Pair<Instance, Double>(instance, new Double(boundaryOfCurrentInstance)));
		}
		IDataset<IInstance> dataset = WekaInstancesUtil.wekaInstancesToDataset(instances);
		ArrayList<Pair<I, Double>> probabilityBoundaries = new ArrayList<Pair<I, Double>>();
		int iterator = 0;
		for(IInstance instance: dataset) {
			probabilityBoundaries.add(new Pair<I, Double>((I) instance, instanceProbabilityBoundaries.get(iterator).getY()));
		}
		return probabilityBoundaries;
	}
}
