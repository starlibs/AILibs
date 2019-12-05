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
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.SimpleRandomSampling;

/**
 * This splitter just creates random split without looking at the data.
 *
 * @author fmohr
 *
 * @param <D>
 */
public class RandomHoldoutSplitter<D extends IDataset<?>> implements IRandomDatasetSplitter<D>, IDatasetSplitSetGenerator<D>, ILoggingCustomizable {

	private final Random rand;
	private final double[] portions;
	private final double portionSum;

	private Logger logger = LoggerFactory.getLogger(RandomHoldoutSplitter.class);

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
		this.logger.info("Creating new split with {} folds.", this.portions.length);
		List<D> folds = new ArrayList<>(this.portions.length);

		/* create sub-indices for the respective folds */
		int totalItems = data.size();
		try {
			D copy = (D)data.createCopy();
			double remainingMass = 1;
			for (int i = 0; i < this.portions.length; i++) {
				double portion = i < this.portions.length ? this.portions[i] : remainingMass;
				remainingMass -= portion;
				if (remainingMass > 0) {
					SimpleRandomSampling<D> subSampler = new SimpleRandomSampling<>(random, copy);
					int sampleSize = (int)Math.round(portion * totalItems);
					subSampler.setSampleSize(sampleSize);
					this.logger.debug("Computing fold of size {}/{}, i.e. a portion of {}", sampleSize, totalItems, portion);
					D fold = subSampler.call();
					folds.add(fold);
					copy = subSampler.getComplementOfLastSample();
					this.logger.debug("Reduced the data by the fold. Remaining items: {}", copy.size());
				}
				else {
					this.logger.debug("This is the last fold, which exhausts the complete original data, so no more sampling will be conducted.");
					folds.add(copy);
				}
			}
		} catch (AlgorithmTimeoutedException | AlgorithmExecutionCanceledException | AlgorithmException | DatasetCreationException e) {
			throw new SplitFailedException(e);
		}
		if (folds.size() != this.portions.length) {
			throw new IllegalStateException("Needed to generate " + this.portions.length + " folds, but only produced " + folds.size());
		}
		return folds;

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

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
