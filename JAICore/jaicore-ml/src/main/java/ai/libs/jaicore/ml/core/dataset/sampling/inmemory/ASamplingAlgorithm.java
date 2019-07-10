package ai.libs.jaicore.ml.core.dataset.sampling.inmemory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.api4.java.ai.ml.DatasetCreationException;
import org.api4.java.ai.ml.IDataset;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.basic.algorithm.EAlgorithmState;

/**
 * An abstract class for sampling algorithms providing basic functionality of an
 * algorithm.
 *
 * @author wever
 * @author Lukas Brandt
 * @author Felix Weiland
 * @author jnowack
 */
public abstract class ASamplingAlgorithm<I, D extends IDataset<I>> extends AAlgorithm<D, D> implements ISamplingAlgorithm<D> {

	private static final Logger LOG = LoggerFactory.getLogger(ASamplingAlgorithm.class);

	protected Integer sampleSize = null;
	protected D sample = null;

	protected ASamplingAlgorithm(final D input) {
		super(input);
	}

	public void setSampleSize(final int size) {
		this.sampleSize = size;
	}

	@SuppressWarnings("unchecked")
	@Override
	public D call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmTimeoutedException {
		Instant timeoutTime = null;
		if (this.getTimeout().milliseconds() <= 0) {
			LOG.debug("Invalid or no timeout set. There will be no timeout in this algorithm run");
			timeoutTime = Instant.MAX;
		} else {
			timeoutTime = Instant.now().plus(this.getTimeout().milliseconds(), ChronoUnit.MILLIS);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Set timeout to {}", timeoutTime);
			}
		}
		// Check missing or invalid configuration.
		if (this.sampleSize == null) {
			throw new AlgorithmException("No valid sample size specified");
		}
		if (this.sampleSize == 0) {
			LOG.warn("Sample size is 0, so an empty data set is returned!");
			try {
				return (D)this.getInput().createEmpty();
			} catch (DatasetCreationException e) {
				throw new AlgorithmException(e, "Could not create a copy of the dataset.");
			}
		}
		D dataset = this.getInput();
		if (dataset == null || dataset.isEmpty()) {
			throw new AlgorithmException("No dataset or an empty dataset was given as an input.");
		}
		if (dataset.size() < this.sampleSize) {
			throw new AlgorithmException("Specified sample size is bigger than the dataset.");
		} else if (dataset.size() == this.sampleSize) {
			LOG.warn("Sample size and data set size are equal. Returning the original data set");
			// The dataset size is exactly the specified sample size, so just return the
			// whole dataset.
			return dataset;
		} else {
			// Working configuration, so create the actual sample.
			this.setState(EAlgorithmState.CREATED);
			while (this.hasNext()) {
				try {
					this.checkAndConductTermination();
				} catch (AlgorithmTimeoutedException e) {
					throw new AlgorithmException(e.getMessage());
				}
				if (Instant.now().isAfter(timeoutTime)) {
					LOG.warn("Algorithm is running even though it has been timeouted. Cancelling..");
					this.cancel();
					throw new AlgorithmException("Algorithm is running even though it has been timeouted");
				} else {
					this.nextWithException();
				}
			}
			return this.sample;
		}

	}

	protected AlgorithmEvent doInactiveStep() throws AlgorithmException {
		if (this.sample.size() < this.sampleSize) {
			throw new AlgorithmException("Expected sample size was not reached before termination");
		} else {
			return this.terminate();
		}
	}

	/**
	 * Gets the data point contained in the original data that are not part of the
	 * @return
	 * @throws DatasetCreationException
	 */
	public D getComplement() throws DatasetCreationException {

		if (this.sample == null) {
			throw new IllegalStateException("Sample computation has not started yet.");
		}
		D input = this.getInput();

		/* compute frequencies (necessary, because items could occur several times) */
		Map<Object, Integer> frequenciesInInput = new HashMap<>();
		Map<Object, Integer> frequenciesInSubSample = new HashMap<>();
		Map<Object, Integer> frequenciesInComplement = new HashMap<>();
		for (Object instance : input) {
			frequenciesInInput.put(instance, frequenciesInInput.computeIfAbsent(instance, k -> 0) + 1);
			frequenciesInComplement.put(instance, 0);
			frequenciesInSubSample.put(instance, 0);
		}
		for (Object instance : this.sample) {
			frequenciesInSubSample.put(instance, frequenciesInSubSample.computeIfAbsent(instance, k -> 0) + 1); // inserts 0 if, for some reason, the value has not been defined before
		}

		/* now compute complement */
		D complement = (D)input.createEmpty();
		for (I instance : input) {
			int frequencyInComplement = frequenciesInComplement.get(instance);
			if (frequenciesInSubSample.get(instance) + frequencyInComplement < frequenciesInInput.get(instance)) {
				complement.add(instance);
				frequenciesInComplement.put(instance, frequencyInComplement + 1);
			}
		}

		/* check plausibility (sizes should sum up) */
		if (this.sample.size() + complement.size() != input.size()) {
			throw new IllegalStateException("The input set of size " + input.size() + " has been reduced to " + this.sample.size() + " + " + complement.size() + ". This is not plausible.");
		}
		else {
			for (Entry<Object, Integer> instanceWithFrequency : frequenciesInInput.entrySet()) {
				Object inst = instanceWithFrequency.getKey();
				int frequencyNow = frequenciesInSubSample.get(inst) + frequenciesInComplement.get(inst);
				if (instanceWithFrequency.getValue() != frequencyNow) {
					throw new IllegalStateException("Frequency of instance " + inst + " was " + instanceWithFrequency.getValue() + " but is now " + frequencyNow);
				}
			}
		}
		return complement;
	}
}
