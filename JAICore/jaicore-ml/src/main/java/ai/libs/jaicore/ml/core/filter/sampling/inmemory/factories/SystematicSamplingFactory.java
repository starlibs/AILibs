package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Comparator;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.SystematicSampling;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class SystematicSamplingFactory<D extends ILabeledDataset<?>> extends ASampleAlgorithmFactory<D, SystematicSampling<D>> implements IRerunnableSamplingAlgorithmFactory<D, SystematicSampling<D>> {

	private Comparator<IInstance> datapointComparator = null;
	private SystematicSampling<D> previousRun = null;

	/**
	 * Set a custom comparator that will be used to sort the datapoints before
	 * sampling.
	 *
	 * @param datapointComparator
	 *            Comparator for two datapoints.
	 */
	public void setDatapointComparator(final Comparator<IInstance> datapointComparator) {
		this.datapointComparator = datapointComparator;
	}

	@Override
	public void setPreviousRun(final SystematicSampling<D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public SystematicSampling<D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		SystematicSampling<D> systematicSampling;
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
