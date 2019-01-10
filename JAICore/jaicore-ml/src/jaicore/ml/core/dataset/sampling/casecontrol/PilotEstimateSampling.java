package jaicore.ml.core.dataset.sampling.casecontrol;

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
import jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;
import jaicore.ml.core.dataset.sampling.WekaInstancesUtil;
import jaicore.ml.core.dataset.standard.SimpleDataset;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.core.Instance;
import weka.filters.Filter;

public abstract class PilotEstimateSampling <I extends IInstance> extends CaseControlLikeSampling<I> {
	
	protected int preSampleSize;
	protected Classifier pilotEstimator = new Logistic();
	
	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch(this.getState()) {
		case created:
			this.sample = this.getInput().createEmpty();
			IDataset<I> pilotEstimateSample = this.getInput().createEmpty();
			IDataset<I> sampleCopy = this.getInput().createEmpty();
			
			for(I instance: this.getInput()) {
				sampleCopy.add(instance);
			}
			
			HashMap<Object, Integer> classOccurrences = countClassOccurrences(sampleCopy);
			
			// Count number of classes
			int numberOfClasses = classOccurrences.keySet().size();
						
			// Calculate Boundaries that define which Instances is choose for which random number
			probabilityBoundaries = calculateInstanceBoundaries(classOccurrences, numberOfClasses);
			
			double r;
			I choosenInstance;
			for(int i = 0; i < this.preSampleSize; i++) {
				do {
					r = this.rand.nextDouble();
					choosenInstance = null;
					for(int j = 0; j < probabilityBoundaries.size(); j++) {
						if(probabilityBoundaries.get(j).getY().doubleValue() > r) {
							choosenInstance = probabilityBoundaries.get(j).getX();
							break;
						}
					}
					if(choosenInstance == null) {
						choosenInstance = probabilityBoundaries.get(probabilityBoundaries.size() - 1).getX();
					}
				} while(pilotEstimateSample.contains(choosenInstance));
				pilotEstimateSample.add(choosenInstance);
			}
			Instances pilotEstimateInstances = WekaInstancesUtil.datasetToWekaInstances(pilotEstimateSample);
			
			NumericToNominal numericToNominal = new NumericToNominal();
			String[] options = new String[2];
			options[0] = "-R";
			options[1] = "last";
			numericToNominal.setOptions(options);
			numericToNominal.setInputFormat(pilotEstimateInstances);
			
			pilotEstimateInstances = Filter.useFilter(pilotEstimateInstances, numericToNominal);
			
			ArrayList<Pair<Double, Double>> classMapping = new ArrayList<Pair<Double, Double>>();
			boolean classNotInMapping;
			for(Instance in: pilotEstimateInstances) {
				classNotInMapping = true;
				for(Pair<Double, Double> classPair: classMapping) {
					if(in.classValue() == classPair.getX().doubleValue()) {
						classNotInMapping = false;
					}
				}
				if(classNotInMapping) {
					classMapping.add(new Pair<Double, Double>(new Double(in.classValue()), (double) classMapping.size()));
				}
			}
			
			this.pilotEstimator.buildClassifier(pilotEstimateInstances);
			
			probabilityBoundaries = calculateFinalInstanceBoundaries(WekaInstancesUtil.datasetToWekaInstances(sampleCopy),
					this.pilotEstimator);
			
			return this.activate();
		case active:
			if(this.sample.size() < this.sampleSize) {
				do {
					r = this.rand.nextDouble();
					choosenInstance = null;
					for(int i = 0; i < probabilityBoundaries.size(); i++) {
						if(probabilityBoundaries.get(i).getY().doubleValue() > r) {
							choosenInstance = probabilityBoundaries.get(i).getX();
							break;
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
				return this.terminate();
			}
		case inactive:
			if (this.sample.size() < this.sampleSize) {
				throw new RuntimeException("Expected sample size was not reached before termination");
			} else {
				return this.terminate();
			}
		default: 
			throw new IllegalStateException("Unknown algorithm state "+ this.getState());	
		}
	}
	
	abstract ArrayList<Pair<I, Double>> calculateFinalInstanceBoundaries(Instances instances, Classifier pilotEstimator) throws Exception;
}
