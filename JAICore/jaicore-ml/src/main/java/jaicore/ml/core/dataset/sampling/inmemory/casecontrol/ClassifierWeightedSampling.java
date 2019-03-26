package jaicore.ml.core.dataset.sampling.inmemory.casecontrol;

import java.util.Random;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;
import jaicore.ml.core.dataset.sampling.inmemory.WekaInstancesUtil;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.UnsupportedAttributeTypeException;

/**
 * The idea behind this Sampling method is to weight instances depended on the way a pilot estimator p classified them.
 * Instances that p classified right but was unsure contain the most information and are most likely to be chosen. Instances that p
 * is very sure about and Instances that p is quite sure about their actual class and classified them falsely, are medium likely to be chosen. Instances that
 * p is very unsure about their actual class and classified them falsely are not likely to be chosen.
 * Note that any instance still has a base probability to be chosen.
 * 
 * @author noni4
 *
 * @param <I>
 */

public class ClassifierWeightedSampling<I extends IInstance> extends CaseControlLikeSampling<I> {
	
	private Classifier pilotEstimator;
	private EnumeratedIntegerDistribution finalDistribution;
	private double addForRightClassification;
	private double baseValue;
	
	public ClassifierWeightedSampling(Random rand, Instances instances) {
		this.rand = rand;
		this.pilotEstimator = new NaiveBayes();
		try {
			this.pilotEstimator.buildClassifier(instances);
		} catch (Exception e) {
			e.printStackTrace();
		}
		double mid = this.getMean(instances);
		// base probability to be chosen
		this.baseValue = 10 * mid + 1; // arbitrary value, there most likely be better one
		this.addForRightClassification = this.baseValue + 2 * mid; // like this.baseValue
	}

	@Override
	public AlgorithmEvent nextWithException()
			throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		switch(this.getState()) {
		case created:
			this.sample = this.getInput().createEmpty();
			IDataset<I> sampleCopy = this.getInput().createEmpty();
			for(I instance: this.getInput()) {
				sampleCopy.add(instance);
			}
			try {
				this.finalDistribution = calculateFinalInstanceBoundariesWithDiscaring(
						WekaInstancesUtil.datasetToWekaInstances(sampleCopy), pilotEstimator);
				this.finalDistribution.reseedRandomGenerator(this.rand.nextLong());
			} catch (UnsupportedAttributeTypeException e) {
				e.printStackTrace();
			}	
			return this.activate();
		case active:
			I choosenInstance;
			if(this.sample.size() < this.sampleSize) {
				do {
					choosenInstance = this.getInput().get(this.finalDistribution.sample());
				} while(this.sample.contains(choosenInstance));
				this.sample.add(choosenInstance);
				return new SampleElementAddedEvent(getId());
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
	
	private EnumeratedIntegerDistribution calculateFinalInstanceBoundariesWithDiscaring(
			Instances instances, Classifier pilotEstimator) {
		double[] weights = new double[instances.size()];
		for(int i = 0; i < instances.size(); i++) {
			try {
				double clazz = this.pilotEstimator.classifyInstance(instances.get(i));
				if(clazz == instances.get(i).classValue()) {
					weights[i] = this.addForRightClassification - pilotEstimator.distributionForInstance(instances.get(i))[(int) instances.get(i).classValue()];
				}
				else {
					weights[i] = this.baseValue + pilotEstimator.distributionForInstance(instances.get(i))[(int) clazz];
				}
			}
			catch(Exception e) {
				weights[i] = 0;
			}
		}
		int[] indices = IntStream.range(0, this.getInput().size()).toArray();
		EnumeratedIntegerDistribution finalDistribution = new EnumeratedIntegerDistribution(indices, weights);
		return finalDistribution;
	}
	
	private double getMean(Instances instances) { 
    	double sum = 0.0; 
        for(Instance instance : instances)
			try {
				sum += this.pilotEstimator.distributionForInstance(instance)[(int) instance.classValue()];
			} catch (Exception e) {
				e.printStackTrace();
			} 
        return sum/instances.size(); 
    } 
}
