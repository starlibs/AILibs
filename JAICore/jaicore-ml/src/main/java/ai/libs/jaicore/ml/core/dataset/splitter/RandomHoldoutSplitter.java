package ai.libs.jaicore.ml.core.dataset.splitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.IRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.evaluation.execution.IDatasetSplitSet;
import org.api4.java.ai.ml.core.evaluation.execution.IDatasetSplitSetGenerator;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.reconstruction.IReconstructible;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.basic.reconstruction.ReconstructionUtil;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.SimpleRandomSampling;

/**
 * This splitter just creates random split without looking at the data.
 *
 * @author fmohr
 *
 * @param <D>
 */
public class RandomHoldoutSplitter<D extends IDataset<?>> implements IRandomDatasetSplitter<D>, IDatasetSplitSetGenerator<D>, ILoggingCustomizable, IFoldSizeConfigurableRandomDatasetSplitter<D> {

	private final Random rand;
	private final double[] portions;

	private Logger logger = LoggerFactory.getLogger(RandomHoldoutSplitter.class);

	public RandomHoldoutSplitter(final double... portions) {
		this(new Random(), portions);
	}

	public RandomHoldoutSplitter(final Random rand, final double... portions) {
		double portionSum = Arrays.stream(portions).sum();
		if (!(portionSum > 0 && portionSum <= 1.0)) {
			throw new IllegalArgumentException("The sum of the given portions must not be less or equal 0 or larger than 1. Given portions: " + Arrays.toString(portions));
		}
		this.rand = rand;
		if (portionSum == 1) {
			this.portions = portions;
		} else {
			this.portions = Arrays.copyOf(portions, portions.length + 1);
			this.portions[portions.length] = 1 - portionSum;
		}
	}

	public static <D extends IDataset<?>> List<D> createSplit(final D data, final long seed, final double... portions) throws SplitFailedException, InterruptedException {
		return createSplit(data, seed, LoggerFactory.getLogger(RandomHoldoutSplitter.class), portions);
	}

	/**
	 * This static method exists to enable reproducibility.
	 *
	 * @param <D>
	 * @param data
	 * @param seed
	 * @param logger
	 * @param portions
	 * @return
	 * @throws SplitFailedException
	 * @throws InterruptedException
	 */
	public static <D extends IDataset<?>> List<D> createSplit(final D data, final long seed, final Logger logger, final double... pPortions) throws SplitFailedException, InterruptedException {
		double portionsSum = Arrays.stream(pPortions).sum();
		if (portionsSum > 1) {
			throw new IllegalArgumentException("Sum of portions must not be greater than 1.");
		}

		final double[] portions;
		if (portionsSum < 1.0 - 1E-8) {
			portions = new double[pPortions.length + 1];
			IntStream.range(0, pPortions.length).forEach(x -> portions[x] = pPortions[x]);
			portions[portions.length - 1] = 1.0 - portionsSum;
		} else {
			portions = pPortions;
		}
		logger.info("Creating new split with {} folds.", portions.length);
		List<D> folds = new ArrayList<>(portions.length);

		/* create sub-indices for the respective folds */
		int totalItems = data.size();
		try {
			D copy = (D) data.createCopy();
			Collections.shuffle(copy, new Random(seed));
			double remainingMass = 1;
			for (int numFold = 0; numFold < portions.length; numFold++) {
				double portion = numFold < portions.length ? portions[numFold] : remainingMass;
				remainingMass -= portion;
				if (remainingMass > 0) {
					SimpleRandomSampling<D> subSampler = new SimpleRandomSampling<>(new Random(seed), copy);
					int sampleSize = (int) Math.round(portion * totalItems);
					subSampler.setSampleSize(sampleSize);
					logger.debug("Computing fold of size {}/{}, i.e. a portion of {}", sampleSize, totalItems, portion);
					D fold = subSampler.call();
					addReconstructionInfo(data, fold, seed, numFold, portions);
					folds.add(fold);
					copy = subSampler.getComplementOfLastSample();
					logger.debug("Reduced the data by the fold. Remaining items: {}", copy.size());
				} else {
					logger.debug("This is the last fold, which exhausts the complete original data, so no more sampling will be conducted.");
					folds.add(copy);
					addReconstructionInfo(data, copy, seed, numFold, portions);
				}
			}
		} catch (AlgorithmTimeoutedException | AlgorithmExecutionCanceledException | AlgorithmException | DatasetCreationException e) {
			throw new SplitFailedException(e);
		}
		if (folds.size() != portions.length) {
			throw new IllegalStateException("Needed to generate " + portions.length + " folds, but only produced " + folds.size());
		}
		return folds;
	}

	private static void addReconstructionInfo(final IDataset<?> data, final IDataset<?> fold, final long seed, final int numFold, final double[] portions) {
		if (data instanceof IReconstructible && ReconstructionUtil.areInstructionsNonEmptyIfReconstructibilityClaimed(data)) { // make data reconstructible, but only if the given data is already reconstructible
			((IReconstructible) data).getConstructionPlan().getInstructions().forEach(((IReconstructible) fold)::addInstruction);
			((IReconstructible) fold).addInstruction(
					new ReconstructionInstruction(RandomHoldoutSplitter.class.getName(), "getFoldOfSplit", new Class<?>[] { IDataset.class, long.class, int.class, double[].class }, new Object[] { "this", seed, numFold, portions }));
		}
	}

	public static <D extends IDataset<?>> D getFoldOfSplit(final D data, final long seed, final int fold, final double... portions) throws SplitFailedException, InterruptedException {
		return createSplit(data, seed, portions).get(fold);
	}

	@Override
	public List<D> split(final D data, final Random random) throws SplitFailedException, InterruptedException {
		return createSplit(data, this.rand.nextLong(), this.logger, this.portions);
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

	@Override
	public String toString() {
		return "RandomHoldoutSplitter [rand=" + this.rand + ", portions=" + Arrays.toString(this.portions) + "]";
	}

	@Override
	public List<D> split(final D data, final Random random, final double... relativeFoldSizes) throws SplitFailedException, InterruptedException {
		return createSplit(data, random.nextLong(), this.logger, relativeFoldSizes);
	}

}
