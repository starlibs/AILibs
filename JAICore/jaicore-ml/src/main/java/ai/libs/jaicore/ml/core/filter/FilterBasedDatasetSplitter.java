package ai.libs.jaicore.ml.core.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.splitter.IDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.core.filter.unsupervised.sampling.ISamplingAlgorithm;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;

public class FilterBasedDatasetSplitter<D extends IDataset<?>> implements IDatasetSplitter<D>, IFoldSizeConfigurableRandomDatasetSplitter<D> {

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
		return this.split(data, this.random, this.relSampleSize);
	}

	@Override
	public int getNumberOfFoldsPerSplit() {
		return 2;
	}

	@Override
	public List<D> split(final D data, final Random random, final double... relativeFoldSizes) throws SplitFailedException, InterruptedException {
		Objects.requireNonNull(data);
		if (data.isEmpty()) {
			throw new IllegalArgumentException("Cannot split empty dataset.");
		}
		if (relativeFoldSizes.length > 2 || relativeFoldSizes.length == 2 && relativeFoldSizes[0] + relativeFoldSizes[1] != 1) {
			throw new IllegalArgumentException("Invalid fold size specification " + Arrays.toString(relativeFoldSizes));
		}
		int size = (int) Math.round(data.size() * relativeFoldSizes[0]);
		ISamplingAlgorithm<D> sampler = this.samplerFactory.getAlgorithm(size, data, random);
		try {
			D firstFold = sampler.nextSample();
			D secondFold = sampler.getComplementOfLastSample();
			return Arrays.asList(firstFold, secondFold);
		} catch (DatasetCreationException e) {
			throw new SplitFailedException(e);
		}
	}
}
