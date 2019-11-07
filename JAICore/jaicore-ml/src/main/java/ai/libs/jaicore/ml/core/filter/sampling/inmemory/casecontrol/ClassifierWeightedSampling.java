package ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol;

import java.util.Random;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationDataset;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassifier;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.filter.sampling.SampleElementAddedEvent;

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

public class ClassifierWeightedSampling<D extends ISingleLabelClassificationDataset> extends CaseControlLikeSampling<ISingleLabelClassificationInstance, D> {

	private Logger logger = LoggerFactory.getLogger(ClassifierWeightedSampling.class);

	private ISingleLabelClassifier pilotEstimator;
	private EnumeratedIntegerDistribution finalDistribution;
	private double addForRightClassification;
	private double baseValue;

	public ClassifierWeightedSampling(final ISingleLabelClassifier pilotEstimator, final Random rand, final ISingleLabelClassificationDataset dataset, final D input) {
		super(input);
		this.rand = rand;
		this.pilotEstimator = pilotEstimator;
		try {
			this.pilotEstimator.fit(dataset);
		} catch (Exception e) {
			this.logger.error("Cannot build pilot estimator", e);
		}
		double mid = this.getMean(dataset);
		// base probability to be chosen
		this.baseValue = 10 * mid + 1; // arbitrary value, there most likely be better one
		this.addForRightClassification = this.baseValue + 2 * mid; // like this.baseValue
	}

	@SuppressWarnings("unchecked")
	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			try {
				this.sample = (D) this.getInput().createEmptyCopy();
				D sampleCopy = (D) this.getInput().createEmptyCopy();
				for (ISingleLabelClassificationInstance instance : this.getInput()) {
					sampleCopy.add(instance);
				}
				this.finalDistribution = this.calculateFinalInstanceBoundariesWithDiscaring(sampleCopy, this.pilotEstimator);
				this.finalDistribution.reseedRandomGenerator(this.rand.nextLong());
			} catch (DatasetCreationException e) {
				throw new AlgorithmException("Could not create a copy of the dataset.", e);
			}
			return this.activate();
		case ACTIVE:
			ISingleLabelClassificationInstance choosenInstance;
			if (this.sample.size() < this.sampleSize) {
				do {
					choosenInstance = this.getInput().get(this.finalDistribution.sample());
				} while (this.sample.contains(choosenInstance));
				this.sample.add(choosenInstance);
				return new SampleElementAddedEvent(this.getId());
			} else {
				return this.terminate();
			}
		case INACTIVE:
			this.doInactiveStep();
			break;
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
		return null;
	}

	private EnumeratedIntegerDistribution calculateFinalInstanceBoundariesWithDiscaring(final ISingleLabelClassificationDataset instances, final ISingleLabelClassifier pilotEstimator) {
		double[] weights = new double[instances.size()];
		for (int i = 0; i < instances.size(); i++) {
			try {
				ISingleLabelClassification prediction = this.pilotEstimator.predict(instances.get(i));
				if (prediction.getLabelWithHighestProbability() == instances.get(i).getIntLabel()) {
					weights[i] = this.addForRightClassification - prediction.getProbabilityOfLabel(instances.get(i).getLabel());
				} else {
					weights[i] = this.baseValue + prediction.getProbabilityOfLabel(prediction.getLabelWithHighestProbability());
				}
			} catch (Exception e) {
				weights[i] = 0;
			}
		}
		int[] indices = IntStream.range(0, this.getInput().size()).toArray();
		return new EnumeratedIntegerDistribution(indices, weights);
	}

	private double getMean(final ISingleLabelClassificationDataset instances) {
		double sum = 0.0;
		for (ISingleLabelClassificationInstance instance : instances) {
			try {
				sum += this.pilotEstimator.predict(instance).getProbabilityOfLabel(instance.getLabel());
			} catch (Exception e) {
				this.logger.error("Unexpected error in pilot estimator", e);
			}
		}
		return sum / instances.size();
	}
}
