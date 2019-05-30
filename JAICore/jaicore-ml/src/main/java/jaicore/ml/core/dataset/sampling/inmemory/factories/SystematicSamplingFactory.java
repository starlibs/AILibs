package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Comparator;
import java.util.Random;

import jaicore.ml.core.dataset.INumericArrayInstance;
import jaicore.ml.core.dataset.IOrderedDataset;
import jaicore.ml.core.dataset.sampling.inmemory.SystematicSampling;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class SystematicSamplingFactory<I extends INumericArrayInstance, D extends IOrderedDataset<I>> implements IRerunnableSamplingAlgorithmFactory<D, SystematicSampling<I, D>> {

	private Comparator<I> datapointComparator = null;
	private SystematicSampling<I, D> previousRun = null;

	/**
	 * Set a custom comparator that will be used to sort the datapoints before
	 * sampling.
	 * 
	 * @param datapointComparator
	 *            Comparator for two datapoints.
	 */
	public void setDatapointComparator(Comparator<I> datapointComparator) {
		this.datapointComparator = datapointComparator;
	}

	@Override
	public void setPreviousRun(SystematicSampling<I, D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public SystematicSampling<I, D> getAlgorithm(int sampleSize, D inputDataset, Random random) {
		SystematicSampling<I, D> systematicSampling;
		if (this.datapointComparator != null) {
			systematicSampling = new SystematicSampling<>(random, this.datapointComparator, inputDataset);
		} else {
			systematicSampling = new SystematicSampling<>(random, inputDataset);
		}
		systematicSampling.setSampleSize(sampleSize);
		if (previousRun != null && previousRun.getSortedDataset() != null) {
			systematicSampling.setSortedDataset(previousRun.getSortedDataset());
		}
		return systematicSampling;
	}

}
