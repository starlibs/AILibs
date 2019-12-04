package ai.libs.jaicore.ml.core.dataset.splitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.splitter.IRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.evaluation.execution.IDatasetSplitSet;
import org.api4.java.ai.ml.core.evaluation.execution.IDatasetSplitSetGenerator;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.SimpleRandomSampling;

/**
 * This splitter just creates random split without looking at the data.
 *
 * @author fmohr
 *
 * @param <D>
 */
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
			throw new IllegalArgumentException("The sum of the given portions must not be less or equal 0 or larger than 1. Given portions: " + Arrays.toString(portions));
		}
		this.portionSum = portionSum;
		this.rand = rand;
		if (portionSum == 1) {
			this.portions = portions;
		}
		else {
			this.portions = new double[portions.length + 1];
			for (int i = 0; i < portions.length; i++) {
				this.portions[i] = portions[i];
			}
			this.portions[portions.length] = 1 - portionSum;
		}
	}

	@Override
	public List<D> split(final D data, final Random random) throws SplitFailedException, InterruptedException {
		List<D> holdOutSplits = new ArrayList<>(this.portions.length);

		/* create sub-indices for the respective folds */
		D copy;
		int totalItems = data.size();
		try {
			copy = (D)data.createCopy();
			int index = 0;
			double remainingMass = 1;
			for (int i = 0; i < this.portions.length; i++) {
				double portion = i < this.portions.length ? this.portions[i] : remainingMass;
				remainingMass -= portion;
				SimpleRandomSampling subSampler = new SimpleRandomSampling(random, copy);
				subSampler.setSampleSize((int)Math.round(portion * totalItems));
				holdOutSplits.add((D)subSampler.call());
				copy = (D)subSampler.getComplementOfLastSample();

			}
		} catch (AlgorithmTimeoutedException | AlgorithmExecutionCanceledException | AlgorithmException | DatasetCreationException e) {
			throw new SplitFailedException(e);
		}
		if (holdOutSplits.size() != this.portions.length) {
			throw new IllegalStateException("Needed to generate " + this.portions.length + " folds, but only produced " + holdOutSplits.size());
		}
		return holdOutSplits;

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
