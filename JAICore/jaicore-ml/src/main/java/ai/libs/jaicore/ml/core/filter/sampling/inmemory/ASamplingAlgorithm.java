package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.core.filter.unsupervised.sampling.ISamplingAlgorithm;
import org.api4.java.algorithm.events.IAlgorithmEvent;
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
public abstract class ASamplingAlgorithm<D extends IDataset<?>> extends AAlgorithm<D, D> implements ISamplingAlgorithm<D> {

	private static final Logger LOG = LoggerFactory.getLogger(ASamplingAlgorithm.class);

	protected int sampleSize = -1;
	protected D sample = null;

	private class Caps<I extends IInstance> {
		private IDataset<I> cloneOfOriginal; // this is also a D (so casts are admissible)
		private IDataset<I> dataForSample; // this is also a D (so casts are admissible)

		public Caps(final IDataset<I> clone) {
			super();
			this.cloneOfOriginal = clone;
		}

		private void computeSample() throws AlgorithmException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
			Instant timeoutTime = null;
			if (ASamplingAlgorithm.this.getTimeout().milliseconds() <= 0) {
				LOG.debug("Invalid or no timeout set. There will be no timeout in this algorithm run");
				timeoutTime = Instant.MAX;
			} else {
				timeoutTime = Instant.now().plus(ASamplingAlgorithm.this.getTimeout().milliseconds(), ChronoUnit.MILLIS);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Set timeout to {}", timeoutTime);
				}
			}
			// Check missing or invalid configuration.
			if (ASamplingAlgorithm.this.sampleSize == -1) {
				throw new AlgorithmException("No valid sample size specified");
			}
			if (ASamplingAlgorithm.this.sampleSize == 0) {
				LOG.warn("Sample size is 0, so an empty data set is returned!");
				try {
					this.dataForSample = (IDataset<I>) ASamplingAlgorithm.this.getInput().createEmptyCopy();
					return;
				} catch (DatasetCreationException e) {
					throw new AlgorithmException("Could not create a copy of the dataset.", e);
				}
			}
			D dataset = ASamplingAlgorithm.this.getInput();
			if (dataset == null || dataset.isEmpty()) {
				throw new AlgorithmException("No dataset or an empty dataset was given as an input.");
			}
			if (dataset.size() < ASamplingAlgorithm.this.sampleSize) {
				throw new AlgorithmException("Specified sample size is bigger than the dataset.");
			} else if (dataset.size() == ASamplingAlgorithm.this.sampleSize) {
				LOG.warn("Sample size and data set size are equal. Returning the original data set");
				// The dataset size is exactly the specified sample size, so just return the
				// whole dataset.
				this.dataForSample = (IDataset<I>) dataset;
			} else {
				// Working configuration, so create the actual sample.
				ASamplingAlgorithm.this.setState(EAlgorithmState.CREATED);
				while (ASamplingAlgorithm.this.hasNext()) {
					try {
						ASamplingAlgorithm.this.checkAndConductTermination();
					} catch (AlgorithmTimeoutedException e) {
						throw new AlgorithmException(e.getMessage());
					}
					if (Instant.now().isAfter(timeoutTime)) {
						LOG.warn("Algorithm is running even though it has been timeouted. Cancelling..");
						ASamplingAlgorithm.this.cancel();
						throw new AlgorithmException("Algorithm is running even though it has been timeouted");
					} else {
						ASamplingAlgorithm.this.nextWithException();
					}
				}
				this.dataForSample = (IDataset<I>) ASamplingAlgorithm.this.sample;
			}
		}

		public IDataset<I> getComplement() throws DatasetCreationException, InterruptedException {
			return new SampleComplementComputer().getComplement(this.cloneOfOriginal, this.dataForSample);
		}
	}

	private final Caps<?> caps;

	protected ASamplingAlgorithm(final D input) {
		this(input, (Class<? extends IInstance>) input.get(0).getClass());
	}

	protected <I extends IInstance> ASamplingAlgorithm(final D input, final Class<I> instanceClass) {
		super(input);
		IDataset<I> dsCopy = (IDataset<I>) input;
		if (!instanceClass.isInstance(input.get(0))) {
			throw new IllegalArgumentException("The class " + instanceClass.getName() + " is not a valid cast for the given dataset.");
		}
		this.caps = new Caps<>(dsCopy);
	}

	public void setSampleSize(final int size) {
		this.sampleSize = size;
	}

	public void setSampleSize(final double relativeSize) {
		if (relativeSize <= 0 || relativeSize >= 1) {
			throw new IllegalArgumentException("Illegal relative sample size " + relativeSize + ". Must be between 0 and 1 (both exclusive).");
		}
		this.setSampleSize((int)Math.round(this.getInput().size() * relativeSize));
	}

	@SuppressWarnings("unchecked")
	@Override
	public D call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmTimeoutedException {
		this.caps.computeSample();
		return (D) this.caps.dataForSample;
	}

	protected IAlgorithmEvent doInactiveStep() throws AlgorithmException {
		if (this.sample.size() < this.sampleSize) {
			throw new AlgorithmException("Expected sample size was not reached before termination");
		} else {
			return this.terminate();
		}
	}

	@Override
	public D nextSample() throws InterruptedException, DatasetCreationException {
		try {
			return this.call();
		} catch (AlgorithmTimeoutedException | AlgorithmExecutionCanceledException | AlgorithmException e) {
			throw new DatasetCreationException(e);
		}
	}

	/**
	 * Gets the data point contained in the original data that are not part of the
	 *
	 * @return
	 * @throws DatasetCreationException
	 * @throws InterruptedException
	 */
	@Override
	public D getComplementOfLastSample() throws DatasetCreationException, InterruptedException {
		return (D) this.caps.getComplement();
	}

	public int getSampleSize() {
		return this.sampleSize;
	}
}
