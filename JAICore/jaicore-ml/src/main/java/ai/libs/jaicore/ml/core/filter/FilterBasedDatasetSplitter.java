package ai.libs.jaicore.ml.core.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.splitter.IDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.core.filter.unsupervised.sampling.ISamplingAlgorithm;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;

public class FilterBasedDatasetSplitter<D extends IDataset<?>> implements IDatasetSplitter<D> {

	private final ISamplingAlgorithmFactory<D, ?> samplerFactory;
	private double relSampleSize;
	private Random random;

	public FilterBasedDatasetSplitter(final ISamplingAlgorithmFactory<D, ?> samplerFactory, final double relSampleSize, final Random random) {
		super();
		this.samplerFactory = samplerFactory;
		this.relSampleSize = relSampleSize;
		this.random = random;
	}

	@Override
	public List<D> split(final D data) throws SplitFailedException, InterruptedException {
		int size = (int) Math.round(data.size() * this.relSampleSize);
		ISamplingAlgorithm<D> sampler = this.samplerFactory.getAlgorithm(size, data, this.random);
		try {
			D firstFold = sampler.nextSample();
			D secondFold = sampler.getComplementOfLastSample();
			return Arrays.asList(firstFold, secondFold);
		} catch (DatasetCreationException e) {
			throw new SplitFailedException(e);
		}
	}

	@Override
	public int getNumberOfFoldsPerSplit() {
		return 2;
	}

}
