package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Comparator;
import java.util.Random;

import org.api4.java.ai.ml.INumericArrayInstance;
import org.api4.java.ai.ml.IOrderedDataset;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.SystematicSampling;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class SystematicSamplingFactory<I extends INumericArrayInstance, D extends IOrderedDataset<I>> implements IRerunnableSamplingAlgorithmFactory<I, D, SystematicSampling<I, D>> {

	private Comparator<I> datapointComparator = null;
	private SystematicSampling<I, D> previousRun = null;

	/**
	 * Set a custom comparator that will be used to sort the datapoints before
	 * sampling.
	 *
	 * @param datapointComparator
	 *            Comparator for two datapoints.
	 */
	public void setDatapointComparator(final Comparator<I> datapointComparator) {
		this.datapointComparator = datapointComparator;
	}

	@Override
	public void setPreviousRun(final SystematicSampling<I, D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public SystematicSampling<I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		SystematicSampling<I, D> systematicSampling;
		if (this.datapointComparator != null) {
			systematicSampling = new SystematicSampling<>(random, this.datapointComparator, inputDataset);
		} else {
			systematicSampling = new SystematicSampling<>(random, inputDataset);
		}
		systematicSampling.setSampleSize(sampleSize);
		if (this.previousRun != null && this.previousRun.getSortedDataset() != null) {
			systematicSampling.setSortedDataset(this.previousRun.getSortedDataset());
		}
		return systematicSampling;
	}

}
