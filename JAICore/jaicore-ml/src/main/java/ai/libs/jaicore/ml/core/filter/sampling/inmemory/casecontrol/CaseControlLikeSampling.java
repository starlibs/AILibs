package ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.core.dataset.DatasetDeriver;
import ai.libs.jaicore.ml.core.filter.sampling.SampleElementAddedEvent;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;

public abstract class CaseControlLikeSampling<D extends ILabeledDataset<? extends ILabeledInstance>> extends ASamplingAlgorithm<D> {

	protected Random rand;
	protected List<Pair<ILabeledInstance, Double>> acceptanceThresholds = null;
	private final DatasetDeriver<D> deriver;
	private int currentlyConsideredIndex = 0;

	protected CaseControlLikeSampling(final D input) {
		super(input);
		this.deriver = new DatasetDeriver<>(input);
	}

	public List<Pair<ILabeledInstance, Double>> getAcceptanceThresholds() {
		return this.acceptanceThresholds;
	}

	public void setAcceptanceTresholds(final List<Pair<ILabeledInstance, Double>> thresholds) {
		this.acceptanceThresholds = thresholds;
	}

	public abstract List<Pair<ILabeledInstance, Double>> computeAcceptanceThresholds() throws ThresholdComputationFailedException, InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException;

	@Override
	public final IAlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException {
		this.logger.debug("Executing next step.");
		switch (this.getState()) {
		case CREATED:
			try {
				this.acceptanceThresholds = this.computeAcceptanceThresholds();
			} catch (ThresholdComputationFailedException e1) {
				throw new AlgorithmException("Sampler initialization failed due to problems in threshold computation.", e1);
			}
			this.logger.info("Initialized sampler with {} acceptance thresholds and {}x{}-dataset", this.acceptanceThresholds.size(), this.getInput().size(), this.getInput().getNumAttributes());
			return this.activate();
		case ACTIVE:

			/* draw next sample element */
			int i = 0;
			while (this.deriver.currentSizeOfTarget() < this.sampleSize && this.currentlyConsideredIndex < this.acceptanceThresholds.size()) {
				if (i++ % 100 == 0) {
					this.checkAndConductTermination();
				}
				double r = this.rand.nextDouble();
				this.currentlyConsideredIndex ++;
				if (this.acceptanceThresholds.get(this.currentlyConsideredIndex - 1).getY().doubleValue() >= r) {
					this.deriver.add(this.currentlyConsideredIndex - 1);
					return new SampleElementAddedEvent(this);
				}
			}

			/* if no more samples can or shall be drawn, create the sample */
			try {
				this.sample = this.deriver.build();
			} catch (DatasetCreationException e) {
				throw new AlgorithmException("Could not create split.", e);
			}
			this.logger.info("Sampling has finished. Shutting down sampling algorithm.");
			return this.doInactiveStep();
		default:
			throw new IllegalStateException("No actions defined for algorithm state " + this.getState());
		}
	}

	/**
	 * Count occurrences of every class. Needed to determine the probability for all
	 * instances of that class
	 *
	 * @param dataset
	 *            Dataset of the sample algorithm object
	 * @return HashMap of occurrences
	 * @throws ClassNotFoundException
	 */
	protected HashMap<Object, Integer> countClassOccurrences(final D dataset) {
		HashMap<Object, Integer> classOccurrences = new HashMap<>();
		for (ILabeledInstance instance : dataset) {
			boolean classExists = false;
			for (Object clazz : classOccurrences.keySet()) {
				if (clazz.equals(instance.getLabel())) {
					classExists = true;
				}
			}
			if (classExists) {
				classOccurrences.put(instance.getLabel(), classOccurrences.get(instance.getLabel()).intValue() + 1);
			} else {
				classOccurrences.put(instance.getLabel(), 0);
			}
		}
		return classOccurrences;
	}
}
