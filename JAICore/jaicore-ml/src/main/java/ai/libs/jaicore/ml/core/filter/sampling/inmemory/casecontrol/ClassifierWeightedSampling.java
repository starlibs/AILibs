package ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;

/**
 * The idea behind this Sampling method is to weight instances depended on the
 * way a pilot estimator p classified them. Instances that p classified right
 * but was unsure contain the most information and are most likely to be chosen.
 * Instances that p is very sure about and Instances that p is quite sure about
 * their actual class and classified them falsely, are medium likely to be
 * chosen. Instances that p is very unsure about their actual class and
 * classified them falsely are not likely to be chosen. Note that any instance
 * still has a base probability to be chosen.
 *
 * @author noni4
 *
 * @param <I>
 */

public class ClassifierWeightedSampling<D extends ILabeledDataset<? extends ILabeledInstance>> extends PilotEstimateSampling<D> {

	private Logger logger = LoggerFactory.getLogger(ClassifierWeightedSampling.class);

	public ClassifierWeightedSampling(final IClassifier pilotEstimator, final Random rand, final D dataset) {
		super(dataset, pilotEstimator);
		this.rand = rand;
	}

	private double getMean(final ILabeledDataset<?> instances) {
		double sum = 0.0;
		for (ILabeledInstance instance : instances) {
			try {
				sum += this.getPilotEstimator().predict(instance).getProbabilityOfLabel(instance.getLabel());
			} catch (Exception e) {
				this.logger.error("Unexpected error in pilot estimator", e);
			}
		}
		return sum / instances.size();
	}

	@Override
	public List<Pair<ILabeledInstance, Double>> calculateAcceptanceThresholdsWithTrainedPilot(final D dataset, final IClassifier pilot) {

		/* compute mean value and base values the instances must have */
		double mid = this.getMean(dataset);
		double baseValue = 10 * mid + 1; // arbitrary value, there most likely be better one
		double addForRightClassification = baseValue + 2 * mid; // like baseValue

		/* determine probability for each index to be chosen */
		double[] weights = new double[dataset.size()];
		for (int i = 0; i < weights.length; i++) {
			try {
				IPrediction prediction = pilot.predict(dataset.get(i));
				if (prediction.getLabelWithHighestProbability() == dataset.get(i).getLabel()) {
					weights[i] = addForRightClassification - prediction.getProbabilityOfLabel(dataset.get(i).getLabel());
				} else {
					weights[i] = baseValue + prediction.getProbabilityOfLabel(prediction.getLabelWithHighestProbability());
				}
			} catch (Exception e) {
				weights[i] = 0;
			}
		}
		int[] indices = IntStream.range(0, this.getInput().size()).toArray();
		EnumeratedIntegerDistribution finalDistribution = new EnumeratedIntegerDistribution(indices, weights);
		finalDistribution.reseedRandomGenerator(this.rand.nextLong());

		/* now draw <number of samples> many indices whose threshold will be set to 1 */
		int n = this.getSampleSize();
		Set<Integer> consideredIndices = new HashSet<>();
		for (int i = 0; i < n; i++) {
			int index;
			do {
				index = finalDistribution.sample();
			} while (consideredIndices.contains(index));
			consideredIndices.add(index);
		}

		/* now create the list of pairs */
		List<Pair<ILabeledInstance, Double>> thresholds = new ArrayList<>();
		int m = dataset.size();
		for (int i = 0; i < m; i++) {
			ILabeledInstance inst = dataset.get(i);
			double threshold = consideredIndices.contains(i) ? 1 : 0;
			thresholds.add(new Pair<>(inst, threshold));
		}
		return thresholds;
	}
}
