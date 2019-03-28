package jaicore.ml.core.dataset.sampling.inmemory.casecontrol;

import java.util.ArrayList;
import java.util.Random;

import jaicore.basic.sets.SetUtil.Pair;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import jaicore.ml.core.dataset.*;
import jaicore.ml.core.dataset.sampling.inmemory.WekaInstancesUtil;

public class LocalCaseControlSampling<I extends IInstance> extends PilotEstimateSampling<I> {

	public LocalCaseControlSampling(Random rand, int preSampleSize, IDataset<I> input) {
		super(input);
		this.rand = rand;
		this.preSampleSize = preSampleSize;
	}

	protected ArrayList<Pair<I, Double>> calculateFinalInstanceBoundaries(Instances instances,
			Classifier pilotEstimator) {
		double boundaryOfCurrentInstance = 0.0;
		ArrayList<Pair<Instance, Double>> instanceProbabilityBoundaries = new ArrayList<Pair<Instance, Double>>();
		double sumOfDistributionLosses = 0;
		double loss;
		for (Instance instance : instances) {
			try {
				loss = 1 - pilotEstimator.distributionForInstance(instance)[(int) instance.classValue()];
			} catch (Exception e) {
				loss = 1;
			}
			sumOfDistributionLosses += loss;
		}
		for (Instance instance : instances) {
			try {
				loss = 1 - pilotEstimator.distributionForInstance(instance)[(int) instance.classValue()];
			} catch (Exception e) {
				loss = 1;
			}
			boundaryOfCurrentInstance += loss / sumOfDistributionLosses;
			instanceProbabilityBoundaries
					.add(new Pair<Instance, Double>(instance, new Double(boundaryOfCurrentInstance)));
		}
		IDataset<IInstance> dataset = WekaInstancesUtil.wekaInstancesToDataset(instances);
		ArrayList<Pair<I, Double>> probabilityBoundaries = new ArrayList<Pair<I, Double>>();
		int iterator = 0;
		for (IInstance instance : dataset) {
			probabilityBoundaries
					.add(new Pair<I, Double>((I) instance, instanceProbabilityBoundaries.get(iterator).getY()));
			iterator++;
		}
		return probabilityBoundaries;
	}
}
