package ai.libs.jaicore.ml.core.filter;

import java.util.ArrayList;
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
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.basic.reconstruction.ReconstructionUtil;
import ai.libs.jaicore.ml.core.dataset.splitter.ReproducibleSplit;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;

public class FilterBasedDatasetSplitter<D extends IDataset<?>> implements IDatasetSplitter<D>, IFoldSizeConfigurableRandomDatasetSplitter<D>, ILoggingCustomizable {

	private final ISamplingAlgorithmFactory<D, ?> samplerFactory;
	private double relSampleSize;
	private Random random;
	private Logger logger = LoggerFactory.getLogger(FilterBasedDatasetSplitter.class);

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
		return getSplit(data, this.samplerFactory, random.nextLong(), this.logger, relativeFoldSizes);
	}

	public static <D extends IDataset<?>> List<D> getSplit(final D data, final ISamplingAlgorithmFactory<D, ?> samplerFactory, final long seed, final List<Double> relativeFoldSizes) throws InterruptedException, SplitFailedException {
		if (relativeFoldSizes.size() > 1) {
			return getSplit(data, samplerFactory, seed, relativeFoldSizes.get(0), relativeFoldSizes.get(1));
		}
		else {
			return getSplit(data, samplerFactory, seed, relativeFoldSizes.get(0));
		}
	}

	public static <D extends IDataset<?>> List<D> getSplit(final D data, final ISamplingAlgorithmFactory<D, ?> samplerFactory, final long seed, final double... relativeFoldSizes) throws InterruptedException, SplitFailedException {
		return getSplit(data, samplerFactory, seed, LoggerFactory.getLogger(FilterBasedDatasetSplitter.class), relativeFoldSizes);
	}

	public static <D extends IDataset<?>> List<D> getSplit(final D data, final ISamplingAlgorithmFactory<D, ?> samplerFactory, final long seed, final Logger logger, final double... relativeFoldSizes) throws InterruptedException, SplitFailedException {

		Objects.requireNonNull(data);
		if (data.isEmpty()) {
			throw new IllegalArgumentException("Cannot split empty dataset.");
		}
		if (relativeFoldSizes.length > 2 || relativeFoldSizes.length == 2 && relativeFoldSizes[0] + relativeFoldSizes[1] != 1) {
			throw new IllegalArgumentException("Invalid fold size specification " + Arrays.toString(relativeFoldSizes));
		}
		if (data instanceof IReconstructible && !(samplerFactory instanceof IReconstructible)) {
			throw new IllegalStateException("Given data is reproducible and so should the splitters, but the sampler factory used to create the sampling algorithm is not reproducible.");
		}
		int size = (int) Math.round(data.size() * relativeFoldSizes[0]);
		logger.info("Drawing 2-fold split with size {} for the first fold.", size);
		ISamplingAlgorithm<D> sampler = samplerFactory.getAlgorithm(size, data, new Random(seed));
		if (sampler instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) sampler).setLoggerName(logger.getName() + ".sampler");
		}
		try {
			D firstFold = sampler.nextSample();
			logger.debug("Sample for first fold completed, now computing the complement to fill the second fold.");
			D secondFold = sampler.getComplementOfLastSample();
			logger.info("Fold creation completed. Adding reconstruction information.");
			if (data instanceof IReconstructible) {
				if (!ReconstructionUtil.areInstructionsNonEmptyIfReconstructibilityClaimed(data)) {
					logger.info("Not making the split reproducible since the original data is not reproducible.");
					return Arrays.asList(firstFold, secondFold);
				}
				else {
					List<Double> portionsAsList = new ArrayList<>();
					for (double d : relativeFoldSizes) {
						portionsAsList.add(d);
					}
					List<IReconstructionInstruction> instructions = ((IReconstructible) data).getConstructionPlan().getInstructions();
					instructions.forEach(((IReconstructible)firstFold)::addInstruction);
					ReconstructionInstruction rInstForFirstFold = new ReconstructionInstruction(FilterBasedDatasetSplitter.class.getName(), "getFoldOfSplit", new Class<?>[] {IDataset.class, ISamplingAlgorithmFactory.class, long.class, int.class, List.class}, new Object[] {"this", samplerFactory, seed, 0, portionsAsList});
					((IReconstructible)firstFold).addInstruction(rInstForFirstFold);
					instructions.forEach(((IReconstructible)secondFold)::addInstruction);
					ReconstructionInstruction rInstForSecondFold = new ReconstructionInstruction(FilterBasedDatasetSplitter.class.getName(), "getFoldOfSplit", new Class<?>[] {IDataset.class, ISamplingAlgorithmFactory.class, long.class, int.class, List.class}, new Object[] {"this", samplerFactory, seed, 1, portionsAsList});
					((IReconstructible)secondFold).addInstruction(rInstForSecondFold);
					ReconstructionUtil.requireNonEmptyInstructionsIfReconstructibilityClaimed(firstFold);
					ReconstructionUtil.requireNonEmptyInstructionsIfReconstructibilityClaimed(secondFold);
					ReconstructionInstruction inst = new ReconstructionInstruction(FilterBasedDatasetSplitter.class.getName(), "getSplit", new Class<?>[] {IDataset.class, ISamplingAlgorithmFactory.class, long.class, List.class}, new Object[] {"this", samplerFactory, seed, Arrays.asList(relativeFoldSizes)});
					logger.info("Sampling-based split completed, returning two folds of sizes {} and {}.", firstFold.size(), secondFold.size());
					return new ReproducibleSplit<>(inst, data, firstFold, secondFold);
				}
			}
			logger.info("Sampling-based split completed, returning two folds of sizes {} and {}.", firstFold.size(), secondFold.size());
			return Arrays.asList(firstFold, secondFold);
		} catch (DatasetCreationException e) {
			throw new SplitFailedException(e);
		}
	}

	public static <D extends IDataset<?>> D getFoldOfSplit(final D data, final ISamplingAlgorithmFactory<D, ?> samplerFactory, final long seed, final int fold, final List<Double> relativeFoldSizes) throws InterruptedException, SplitFailedException {
		return getSplit(data, samplerFactory, seed, relativeFoldSizes).get(fold);
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
