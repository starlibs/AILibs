package ai.libs.jaicore.ml.core.dataset.splitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.execution.IDatasetSplitSet;
import org.api4.java.ai.ml.classification.execution.IDatasetSplitSetGenerator;
import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.splitter.IRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

import ai.libs.jaicore.ml.core.dataset.DatasetSplitSet;

public class RandomHoldoutSplitter<D extends IDataset<?>> implements IRandomDatasetSplitter<D>, IDatasetSplitSetGenerator<D> {

	private final Random rand;
	private final double[] portions;
	private final double portionSum;

	public RandomHoldoutSplitter(final double... portions) {
		this(new Random(), portions);
	}

	public RandomHoldoutSplitter(final Random rand, final double... portions) {
		double portionSum = Arrays.stream(portions).sum();
		if (!(portionSum > 0 && portionSum <= 1.0)) {
			throw new IllegalArgumentException("The sum of the given portions must not be less or equal 0 or larger than 1.");
		}
		this.portionSum = portionSum;
		this.rand = rand;
		this.portions = portions;
	}

	@Override
	public List<D> split(final D data, final Random random) throws SplitFailedException, InterruptedException {
		List<D> holdOutSplits = new ArrayList<>();

		try {
			for (int i = 0; i < ((this.portionSum < 1.0) ? this.portions.length + 1 : this.portions.length); i++) {
				holdOutSplits.add((D) data.createEmptyCopy());
			}
		} catch (DatasetCreationException e) {
			throw new SplitFailedException("Could not create empty hold out buckets.", e);
		}

		List<Integer> indices = IntStream.range(0, data.size()).mapToObj(x -> Integer.valueOf(x)).collect(Collectors.toList());

		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public int getNumberOfFoldsPerSplit() {
		return this.portions.length;
	}

	@Override
	public int getNumSplitsPerSet() {
		return 1;
	}

	@Override
	public int getNumFoldsPerSplit() {
		return this.portions.length;
	}

	@Override
	public IDatasetSplitSet<D> nextSplitSet(final D data) throws InterruptedException, SplitFailedException {
		return new DatasetSplitSet<>(Arrays.asList(this.split(data)));
	}
}
