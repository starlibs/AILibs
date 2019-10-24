package ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol;

import java.util.ArrayList;
import java.util.Random;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.basic.sets.Pair;

public class OSMAC<D extends ILabeledDataset<ISingleLabelClassificationInstance>> extends PilotEstimateSampling<ISingleLabelClassificationInstance, D> {

	public OSMAC(final Random rand, final int preSampleSize, final D input) {
		super(input);
		this.rand = rand;
		this.preSampleSize = preSampleSize;
	}

	@Override
	protected ArrayList<Pair<ISingleLabelClassificationInstance, Double>> calculateFinalInstanceBoundaries(final D instances, final ISingleLabelClassifier pilotEstimator) {
		double boundaryOfCurrentInstance = 0.0;
		ArrayList<Pair<ISingleLabelClassificationInstance, Double>> probabilityBoundaries = new ArrayList<>();
		ArrayList<Pair<ISingleLabelClassificationInstance, Double>> instanceProbabilityBoundaries = new ArrayList<>();
		double sumOfDistributionLosses = 0;
		int vectorLength;
		double loss;
		for (ISingleLabelClassificationInstance instance : instances) {
			vectorLength = 0;
			for (double dimensionLength : (Double[]) instance.getAttributes()) {
				vectorLength += dimensionLength;
			}
			try {
				loss = 1 - pilotEstimator.predict(instance).getClassDistribution().get(instance.getIntLabel());
			} catch (Exception e) {
				loss = 1;
			}
			sumOfDistributionLosses += loss * vectorLength;
		}
		for (ISingleLabelClassificationInstance instance : instances) {
			vectorLength = 0;
			for (double dimensionLength : (Double[]) instance.getAttributes()) {
				vectorLength += dimensionLength;
			}
			try {
				loss = 1 - pilotEstimator.predict(instance).getClassDistribution().get(instance.getIntLabel());
			} catch (Exception e) {
				loss = 1;
			}
			boundaryOfCurrentInstance += loss * vectorLength / sumOfDistributionLosses;
			instanceProbabilityBoundaries.add(new Pair<ISingleLabelClassificationInstance, Double>(instance, boundaryOfCurrentInstance));
		}
		int iterator = 0;
		for (ISingleLabelClassificationInstance instance : instances) {
			probabilityBoundaries.add(new Pair<>(instance, instanceProbabilityBoundaries.get(iterator).getY()));
			iterator++;
		}
		return probabilityBoundaries;
	}
}
