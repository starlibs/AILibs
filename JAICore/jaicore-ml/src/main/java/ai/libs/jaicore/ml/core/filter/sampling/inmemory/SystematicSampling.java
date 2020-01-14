package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import java.util.Comparator;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.ml.core.dataset.DatasetDeriver;
import ai.libs.jaicore.ml.core.filter.sampling.SampleElementAddedEvent;

/**
 * Implementation of Systematic Sampling: Sort datapoints and pick every k-th
 * datapoint for the sample.
 *
 * @author Lukas Brandt
 */
public class SystematicSampling<D extends ILabeledDataset<?>> extends ASamplingAlgorithm<D> {

	private DatasetDeriver<D> sampleBuilder;
	private Random random;
	private D sortedDataset = null;
	private int k;
	private int startIndex;
	private int index;

	// Default Comparator to sort datapoints by their vector representation.
	private Comparator<IInstance> datapointComparator = (o1, o2) -> {
		double[] v1 = o1.getPoint();
		double[] v2 = o2.getPoint();
		for (int i = 0; i < Math.min(v1.length, v2.length); i++) {
			int c = Double.compare(v1[i], v2[i]);
			if (c != 0) {
				return c;
			}
		}
		return 0;
	};

	/**
	 * Simple constructor that uses the default datapoint comparator.
	 *
	 * @param random
	 *            Random Object for determining the sampling start point.
	 */
	public SystematicSampling(final Random random, final D input) {
		super(input);
		this.random = random;
	}

	/**
	 * Constructor for a custom datapoint comparator.
	 *
	 * @param random
	 *            Random Object for determining the sampling start point.
	 * @param datapointComparator
	 *            Comparator to sort the dataset.
	 */
	public SystematicSampling(final Random random, final Comparator<IInstance> datapointComparator, final D input) {
		this(random, input);
		this.datapointComparator = datapointComparator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IAlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException {
		switch (this.getState()) {
		case CREATED:
			// Initialize variables and sort dataset.
			try {
				if (this.sortedDataset == null) {
					this.sortedDataset = (D) this.getInput().createCopy();
					this.sortedDataset.sort(this.datapointComparator);
					this.sampleBuilder = new DatasetDeriver<>(this.sortedDataset);
				}
			} catch (DatasetCreationException e) {
				throw new AlgorithmException("Could not create a copy of the dataset.", e);
			}
			this.startIndex = this.random.nextInt(this.sortedDataset.size());
			this.k = this.sortedDataset.size() / this.sampleSize;
			this.index = 0;
			return this.activate();
		case ACTIVE:
			// If the sample size is not reached yet, add the next datapoint from the
			// systematic sampling method.
			if (this.sampleBuilder.currentSizeOfTarget() < this.sampleSize) {
				if (this.index % 100 == 0) {
					this.checkAndConductTermination();
				}
				int e = (this.startIndex + (this.index++) * this.k) % this.sortedDataset.size();
				this.sampleBuilder.add(e);
				return new SampleElementAddedEvent(this);
			} else {
				try {
					this.sample = this.sampleBuilder.build();
				} catch (DatasetCreationException e) {
					throw new AlgorithmException("Could not build the sample.", e);
				}
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

	public D getSortedDataset() {
		return this.sortedDataset;
	}

	public void setSortedDataset(final D sortedDataset) {
		this.sortedDataset = sortedDataset;
	}

}
