package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Comparator;
import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.SystematicSampling;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class SystematicSamplingFactory<I extends IInstance>
		implements IRerunnableSamplingAlgorithmFactory<I, SystematicSampling<I>> {

	private Comparator<I> datapointComparator = null;
	private SystematicSampling<I> previousRun = null;

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
	public void setPreviousRun(SystematicSampling<I> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public SystematicSampling<I> getAlgorithm(int sampleSize, IDataset<I> inputDataset, Random random) {
		SystematicSampling<I> systematicSampling;
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
