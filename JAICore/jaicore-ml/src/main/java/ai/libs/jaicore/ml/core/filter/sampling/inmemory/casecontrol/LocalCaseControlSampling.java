package ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol;

import java.util.ArrayList;
import java.util.Random;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationDataset;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassifier;

import ai.libs.jaicore.basic.sets.Pair;

public class LocalCaseControlSampling extends PilotEstimateSampling<ISingleLabelClassificationInstance, ISingleLabelClassificationDataset> {

	public LocalCaseControlSampling(final Random rand, final int preSampleSize, final ISingleLabelClassificationDataset input) {
		super(input);
		this.rand = rand;
		this.preSampleSize = preSampleSize;
	}

	@Override
	public ArrayList<Pair<ISingleLabelClassificationInstance, Double>> calculateFinalInstanceBoundaries(final ISingleLabelClassificationDataset instances, final ISingleLabelClassifier pilotEstimator) {
		double boundaryOfCurrentInstance = 0.0;
		ArrayList<Pair<ISingleLabelClassificationInstance, Double>> instanceProbabilityBoundaries = new ArrayList<>();
		double sumOfDistributionLosses = 0;
		double loss;
		for (ISingleLabelClassificationInstance instance : instances) {
			try {
				loss = 1 - pilotEstimator.predict(instance).getProbabilityOfLabel(instance.getLabel());// .distributionForInstance(wekaInstance)[(int) wekaInstance.classValue()];
			} catch (Exception e) {
				loss = 1;
			}
			sumOfDistributionLosses += loss;
		}
		for (ISingleLabelClassificationInstance instance : instances) {
			try {
				loss = 1 - pilotEstimator.predict(instance).getClassDistribution()[instance.getIntLabel()];
			} catch (Exception e) {
				loss = 1;
			}
			boundaryOfCurrentInstance += loss / sumOfDistributionLosses;
			instanceProbabilityBoundaries.add(new Pair<ISingleLabelClassificationInstance, Double>(instance, boundaryOfCurrentInstance));
		}
		ArrayList<Pair<ISingleLabelClassificationInstance, Double>> probabilityBoundaries = new ArrayList<>();
		int iterator = 0;
		for (ISingleLabelClassificationInstance instance : instances) {
			probabilityBoundaries.add(new Pair<ISingleLabelClassificationInstance, Double>(instance, instanceProbabilityBoundaries.get(iterator).getY()));
			iterator++;
		}
		return probabilityBoundaries;
	}
}
