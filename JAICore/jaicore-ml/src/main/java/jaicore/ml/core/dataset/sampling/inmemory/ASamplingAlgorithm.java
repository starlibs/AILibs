package jaicore.ml.core.dataset.sampling.inmemory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.EAlgorithmState;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.ml.core.dataset.DatasetCreationException;
import jaicore.ml.core.dataset.IDataset;

/**
 * An abstract class for sampling algorithms providing basic functionality of an
 * algorithm.
 *
 * @author wever
 * @author Lukas Brandt
 * @author Felix Weiland
 * @author jnowack
 */
public abstract class ASamplingAlgorithm<D extends IDataset<?>> extends AAlgorithm<D, D> implements ISamplingAlgorithm<D> {

	private static final Logger LOG = LoggerFactory.getLogger(ASamplingAlgorithm.class);

	protected Integer sampleSize = null;
	protected D sample = null;

	protected ASamplingAlgorithm(D input) {
		super(input);
	}

	public void setSampleSize(int size) {
		this.sampleSize = size;
	}

	@SuppressWarnings("unchecked")
	@Override
	public D call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		Instant timeoutTime = null;
		if (this.getTimeout().milliseconds() <= 0) {
			LOG.debug("Invalid or no timeout set. There will be no timeout in this algorithm run");
			timeoutTime = Instant.MAX;
		} else {
			timeoutTime = Instant.now().plus(getTimeout().milliseconds(), ChronoUnit.MILLIS);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Set timeout to {}", timeoutTime);
			}
		}
		// Check missing or invalid configuration.
		if (sampleSize == null) {
			throw new AlgorithmException("No valid sample size specified");
		}
		if (sampleSize == 0) {
			LOG.warn("Sample size is 0, so an empty data set is returned!");
			try {
				return (D)getInput().createEmpty();
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
					checkAndConductTermination();
				} catch (AlgorithmTimeoutedException e) {
					throw new AlgorithmException(e.getMessage());
				}
				if (Instant.now().isAfter(timeoutTime)) {
					LOG.warn("Algorithm is running even though it has been timeouted. Cancelling..");
					this.cancel();
					throw new AlgorithmException("Algorithm is running even though it has been timeouted");
				} else {
					this.next();
				}
			}
			return sample;
		}

	}

	protected AlgorithmEvent doInactiveStep() throws AlgorithmException {
		if (this.sample.size() < this.sampleSize) {
			throw new AlgorithmException("Expected sample size was not reached before termination");
		} else {
			return this.terminate();
		}
	}
}
