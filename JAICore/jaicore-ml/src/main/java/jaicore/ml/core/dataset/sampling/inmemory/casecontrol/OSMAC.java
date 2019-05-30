package jaicore.ml.core.dataset.sampling.inmemory.casecontrol;

import java.util.ArrayList;
import java.util.Random;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.ILabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.weka.WekaInstance;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class OSMAC<I extends ILabeledAttributeArrayInstance<?>, D extends IDataset<I>> extends PilotEstimateSampling<I, D> {

	public OSMAC(Random rand, int preSampleSize, D input) {
		super(input);
		this.rand = rand;
		this.preSampleSize = preSampleSize;
	}

	@Override
	protected ArrayList<Pair<I, Double>> calculateFinalInstanceBoundaries(D instances, Classifier pilotEstimator) {
		double boundaryOfCurrentInstance = 0.0;
		ArrayList<Pair<I, Double>> probabilityBoundaries = new ArrayList<>();
		ArrayList<Pair<I, Double>> instanceProbabilityBoundaries = new ArrayList<>();
		double sumOfDistributionLosses = 0;
		int vectorLength;
		double loss;
		for (I instance : instances) {
			Instance wekaInstance = ((WekaInstance<?>)instance).getElement();
			vectorLength = 0;
			for (double dimensionLength : wekaInstance.toDoubleArray()) {
				vectorLength += dimensionLength;
			}
			try {
				loss = 1 - pilotEstimator.distributionForInstance(wekaInstance)[(int) wekaInstance.classValue()];
			} catch (Exception e) {
				loss = 1;
			}
			sumOfDistributionLosses += loss * vectorLength;
		}
		for (I instance : instances) {
			Instance wekaInstance = ((WekaInstance<?>)instance).getElement();
			vectorLength = 0;
			for (double dimensionLength : wekaInstance.toDoubleArray()) {
				vectorLength += dimensionLength;
			}
			try {
				loss = 1 - pilotEstimator.distributionForInstance(wekaInstance)[(int) wekaInstance.classValue()];
			} catch (Exception e) {
				loss = 1;
			}
			boundaryOfCurrentInstance += loss * vectorLength / sumOfDistributionLosses;
			instanceProbabilityBoundaries.add(new Pair<I, Double>(instance, boundaryOfCurrentInstance));
		}
		int iterator = 0;
		for (I instance : instances) {
			probabilityBoundaries.add(new Pair<>(instance, instanceProbabilityBoundaries.get(iterator).getY()));
			iterator++;
		}
		return probabilityBoundaries;
	}
}
