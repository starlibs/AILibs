package ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.sets.Pair;

public class LocalCaseControlSampling extends PilotEstimateSampling<ILabeledDataset<?>> {

	public LocalCaseControlSampling(final Random rand, final int preSampleSize, final ILabeledDataset<?> input, final IClassifier pilot) {
		super(input, pilot);
		this.rand = rand;
		this.preSampleSize = preSampleSize;
	}

	@Override
	public List<Pair<ILabeledInstance, Double>> calculateAcceptanceThresholdsWithTrainedPilot(final ILabeledDataset<?> instances, final IClassifier pilotEstimator) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		double boundaryOfCurrentInstance = 0.0;
		ArrayList<Pair<ILabeledInstance, Double>> instanceProbabilityBoundaries = new ArrayList<>();
		double sumOfDistributionLosses = 0;
		double loss;
		int i = 0;
		for (ILabeledInstance instance : instances) {
			if (i ++ % 100 == 0) {
				this.checkAndConductTermination();
			}
			try {
				loss = 1 - pilotEstimator.predict(instance).getProbabilityOfLabel(instance.getLabel());
			} catch (Exception e) {
				loss = 1;
			}
			sumOfDistributionLosses += loss;
		}
		for (ILabeledInstance instance : instances) {
			if (i ++ % 100 == 0) {
				this.checkAndConductTermination();
			}
			try {
				loss = 1 - pilotEstimator.predict(instance).getProbabilityOfLabel(instance.getLabel());
			} catch (Exception e) {
				loss = 1;
			}
			boundaryOfCurrentInstance += loss / sumOfDistributionLosses;
			instanceProbabilityBoundaries.add(new Pair<>(instance, boundaryOfCurrentInstance));
		}
		ArrayList<Pair<ILabeledInstance, Double>> probabilityBoundaries = new ArrayList<>();
		int iterator = 0;
		for (ILabeledInstance instance : instances) {
			if (iterator % 100 == 0) {
				this.checkAndConductTermination();
			}
			probabilityBoundaries.add(new Pair<>(instance, instanceProbabilityBoundaries.get(iterator).getY()));
			iterator++;
		}
		return probabilityBoundaries;
	}
}
