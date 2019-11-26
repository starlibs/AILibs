package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Comparator;
import java.util.Random;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.SystematicSampling;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class SystematicSamplingFactory<I extends ILabeledInstance & Clusterable, D extends ILabeledDataset<I>> implements IRerunnableSamplingAlgorithmFactory<D, SystematicSampling<I, D>> {

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
