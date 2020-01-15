package ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.filter.unsupervised.sampling.ISamplingAlgorithm;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;

public abstract class PilotEstimateSampling<D extends ILabeledDataset<? extends ILabeledInstance>> extends CaseControlLikeSampling<D> {

	private final ISamplingAlgorithm<D> subSampler;
	protected int preSampleSize;
	private final IClassifier pilotEstimator;

	protected PilotEstimateSampling(final D input, final IClassifier pilotClassifier) {
		this(input, null, 1, pilotClassifier);
	}

	protected PilotEstimateSampling(final D input, final ISamplingAlgorithmFactory<D, ?> subSamplingFactory, final int preSampleSize, final IClassifier pilotClassifier) {
		super(input);
		Objects.requireNonNull(pilotClassifier);
		this.pilotEstimator = pilotClassifier;
		this.preSampleSize = preSampleSize;
		if (subSamplingFactory != null) {
			this.subSampler = subSamplingFactory.getAlgorithm(preSampleSize, input, new Random(0));
		}
		else {
			this.subSampler = null;
		}
	}

	@Override
	public List<Pair<ILabeledInstance, Double>> computeAcceptanceThresholds() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* if the pilot estimator should be trained only on a subset, build this subset now. Otherwise train it on the whole dataset. */
		if (this.subSampler != null) {
			D subSample = this.subSampler.call();
			this.logger.info("Fitting pilot with reduced dataset of {}/{} instances.", subSample.size(), this.getInput().size());
			this.pilotEstimator.fit(subSample);
		}
		else {
			this.logger.info("Fitting pilot with full dataset.");
			this.pilotEstimator.fit(this.getInput());
		}

		return this.calculateAcceptanceThresholdsWithTrainedPilot(this.getInput(), this.pilotEstimator);
	}

	public abstract List<Pair<ILabeledInstance, Double>> calculateAcceptanceThresholdsWithTrainedPilot(D instances, IClassifier pilotEstimator) throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException;

	public IClassifier getPilotEstimator() {
		return this.pilotEstimator;
	}
}
