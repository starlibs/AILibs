package jaicore.ml.core.dataset.sampling.inmemory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * An abstract class for sampling algorithms providing basic functionality of an
 * algorithm.
 *
 * @author wever
 * @author Lukas Brandt
 * @author Felix Weiland
 * @author jnowack
 */
public abstract class ASamplingAlgorithm<I extends IInstance> extends AAlgorithm<IDataset<I>, IDataset<I>> {

	private static Logger LOG = LoggerFactory.getLogger(ASamplingAlgorithm.class);

	protected Integer sampleSize = null;
	protected IDataset<I> sample = null;

	public void setSampleSize(int size) {
		this.sampleSize = size;
	}

	@Override
	public IDataset<I> call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		Instant timeoutTime = null;
		if (this.getTimeout().milliseconds() <= 0) {
			LOG.debug("Invalid or no timeout set. There will be no timeout in this algorithm run");
			timeoutTime = Instant.MAX;
		} else {
			timeoutTime = Instant.now().plus(getTimeout().milliseconds(), ChronoUnit.MILLIS);
			LOG.debug("Set timeout to {}", timeoutTime.toString());
		}
		// Check missing or invalid configuration.
		if (sampleSize == null) {
			throw new AlgorithmException("No valid sample size specified");
		}
		if (sampleSize == 0) {
			LOG.warn("Sample size is 0, so an empty data set is returned!");
			return getInput().createEmpty();
		}
		IDataset<I> dataset = this.getInput();
		if (dataset == null || dataset.size() == 0) {
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
			this.setState(AlgorithmState.created);
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

}
