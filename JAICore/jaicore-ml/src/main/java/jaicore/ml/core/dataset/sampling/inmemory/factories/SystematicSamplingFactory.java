package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Comparator;
import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.SystematicSampling;

public class SystematicSamplingFactory<I extends IInstance> implements ISamplingAlgorithmFactory<I> {

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

	/**
	 * If a previous run of a systematic sampling on the same dataset was performed
	 * it can be passed here and to get the sorted dataset such that is has not to
	 * be sorted again.
	 * 
	 * @param previousRun
	 *            Systematic Sampling algorithm performed on the same dataset.
	 */
	public void setPreviousRun(SystematicSampling<I> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public ASamplingAlgorithm<I> getAlgorithm(int sampleSize, IDataset<I> inputDataset, Random random) {
		SystematicSampling<I> systematicSampling;
		if (this.datapointComparator != null) {
			systematicSampling = new SystematicSampling<>(random, this.datapointComparator);
		} else {
			systematicSampling = new SystematicSampling<>(random);
		}
		systematicSampling.setSampleSize(sampleSize);
		systematicSampling.setInput(inputDataset);
		if (previousRun != null && previousRun.getSortedDataset() != null) {
			systematicSampling.setSortedDataset(previousRun.getSortedDataset());
		}
		return systematicSampling;
	}

}
