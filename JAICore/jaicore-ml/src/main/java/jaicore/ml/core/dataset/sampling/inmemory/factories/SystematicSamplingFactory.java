package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Comparator;
import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.SystematicSampling;

public class SystematicSamplingFactory<I extends IInstance> implements ISamplingAlgorithmFactory<I> {

	private int sampleSize = -1;
	private IDataset<I> inputDataset = null;
	private Random random = new Random();
	private Comparator<I> datapointComparator = null;
	private SystematicSampling<I> previousRun = null;

	@Override
	public void setSampleSize(int sameplSize) {
		this.sampleSize = sameplSize;
	}

	@Override
	public void setInputDataset(IDataset<I> inputDataset) {
		this.inputDataset = inputDataset;
	}

	@Override
	public void setRandom(long seed) {
		this.random.setSeed(seed);
	}

	@Override
	public void setRandom(Random random) {
		this.random = random;
	}

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
	public ASamplingAlgorithm<I> getAlgorithm() throws IllegalStateException {
		if (this.sampleSize == -1) {
			throw new IllegalStateException(
					"Invalid sample size! A size that the sample should have must be specified before creating the sampling algorithm.");
		}
		if (this.inputDataset == null) {
			throw new IllegalStateException(
					"No input given! A input dataset where the sample should be drawn from must be specified before creating the sampling algorithm.");
		}
		SystematicSampling<I> systematicSampling;
		if (this.datapointComparator != null) {
			systematicSampling = new SystematicSampling<>(this.random, this.datapointComparator);
		} else {
			systematicSampling = new SystematicSampling<>(this.random);
		}
		systematicSampling.setSampleSize(this.sampleSize);
		systematicSampling.setInput(this.inputDataset);
		if (previousRun != null && previousRun.getSortedDataset() != null) {
			systematicSampling.setSortedDataset(previousRun.getSortedDataset());
		}
		return systematicSampling;
	}

}
