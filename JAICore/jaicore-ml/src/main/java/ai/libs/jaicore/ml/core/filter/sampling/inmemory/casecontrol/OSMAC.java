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
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;

public class OSMAC<D extends ILabeledDataset<? extends ILabeledInstance>> extends PilotEstimateSampling<D> {

	public OSMAC(final Random rand, final D input, final IClassifier pilot) {
		super(input, pilot);
		this.rand = rand;
	}

	public OSMAC(final Random rand, final D input, final ISamplingAlgorithmFactory<D, ?> subSamplingFactory, final int preSampleSize, final IClassifier pilot) {
		super(input, subSamplingFactory, preSampleSize, pilot);
		this.rand = rand;
	}

	@Override
	public List<Pair<ILabeledInstance, Double>> calculateAcceptanceThresholdsWithTrainedPilot(final D instances, final IClassifier pilotEstimator) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		double boundaryOfCurrentInstance = 0.0;
		ArrayList<Pair<ILabeledInstance, Double>> probabilityBoundaries = new ArrayList<>();
		double sumOfDistributionLosses = 0;
		int vectorLength;
		int n = instances.size();
		double[] normalizedLosses = new double[n];
		for (int i = 0; i < n; i++) {
			if (i % 100 == 0) {
				this.checkAndConductTermination();
			}
			ILabeledInstance instance = instances.get(i);
			vectorLength = 0;
			for (Object attributeVal : instance.getAttributes()) {
				if (!attributeVal.equals("?")) { // just ignore missing values
					if (!(attributeVal instanceof Number)) {
						throw new IllegalArgumentException("Illegal non-double attribute value " + attributeVal);
					}
					vectorLength += Double.valueOf(attributeVal.toString());
				}
			}
			double loss;
			try {
				loss = 1 - pilotEstimator.predict(instance).getProbabilityOfLabel(instance.getLabel());
			} catch (Exception e) {
				loss = 1;
			}
			normalizedLosses[i] = loss * vectorLength;
			sumOfDistributionLosses += normalizedLosses[i];
		}

		for (int i = 0; i < n; i++) {
			if (i % 100 == 0) {
				this.checkAndConductTermination();
			}
			boundaryOfCurrentInstance += normalizedLosses[i] / sumOfDistributionLosses;
			probabilityBoundaries.add(new Pair<>(instances.get(i), boundaryOfCurrentInstance));
		}
		return probabilityBoundaries;
	}
}
