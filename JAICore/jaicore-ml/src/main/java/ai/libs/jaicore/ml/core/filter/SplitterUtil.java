package ai.libs.jaicore.ml.core.filter;

import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.common.reconstruction.IReconstructible;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.ml.core.dataset.splitter.ReproducibleSplit;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.SimpleRandomSamplingFactory;

public class SplitterUtil {

	private SplitterUtil() {
		/* avoids instantiation */
	}

	public static <D extends ILabeledDataset<?>> List<D> getLabelStratifiedTrainTestSplit(final D dataset, final long seed, final double relativeTrainSize) throws SplitFailedException, InterruptedException {
		boolean isReproducible = dataset instanceof IReconstructible;
		List<D> folds = new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<D>(), relativeTrainSize, new Random(seed)).split(dataset);
		if (!isReproducible) {
			return folds;
		}
		try {
			ReconstructionInstruction instruction = new ReconstructionInstruction(SplitterUtil.class.getMethod("getLabelStratifiedTrainTestSplit", ILabeledDataset.class, long.class, double.class), "this", seed, relativeTrainSize);
			return new ReproducibleSplit<>(instruction, dataset, folds.get(0), folds.get(1)); // the folds themselves should be reconstructible already by the splitter
		} catch (NoSuchMethodException | SecurityException e) {
			throw new SplitFailedException(e);
		}
	}

	public static List<ILabeledDataset<?>> getLabelStratifiedTrainTestSplit(final ILabeledDataset<?> dataset, final Random random, final double relativeTrainSize) throws SplitFailedException, InterruptedException {
		return getLabelStratifiedTrainTestSplit(dataset, random.nextLong(), relativeTrainSize);
	}

	public static ILabeledDataset<?> getTrainFoldOfLabelStratifiedTrainTestSplit(final ILabeledDataset<?> dataset, final Random random, final double relativeTrainSize) throws SplitFailedException, InterruptedException{
		return getLabelStratifiedTrainTestSplit(dataset, random, relativeTrainSize).get(0);
	}

	public static ILabeledDataset<?> getTrainFoldOfLabelStratifiedTrainTestSplit(final ILabeledDataset<?> dataset, final long seed, final double relativeTrainSize) throws SplitFailedException, InterruptedException{
		return getLabelStratifiedTrainTestSplit(dataset, seed, relativeTrainSize).get(0);
	}

	public static ILabeledDataset<?> getTestFoldOfLabelStratifiedTrainTestSplit(final ILabeledDataset<?> dataset, final Random random, final double relativeTrainSize) throws SplitFailedException, InterruptedException{
		return getLabelStratifiedTrainTestSplit(dataset, random, relativeTrainSize).get(1);
	}

	public static ILabeledDataset<?> getTestFoldOfLabelStratifiedTrainTestSplit(final ILabeledDataset<?> dataset, final long seed, final double relativeTrainSize) throws SplitFailedException, InterruptedException{
		return getLabelStratifiedTrainTestSplit(dataset, seed, relativeTrainSize).get(1);
	}




	public static List<ILabeledDataset<?>> getSimpleTrainTestSplit(final ILabeledDataset<?> dataset, final long seed, final double relativeTrainSize) throws SplitFailedException, InterruptedException {
		boolean isReproducible = dataset instanceof IReconstructible;
		List<ILabeledDataset<?>> folds = getSimpleTrainTestSplit(dataset, new Random(seed), relativeTrainSize);
		if (!isReproducible) {
			return folds;
		}
		try {
			IReconstructible rDataset = (IReconstructible)dataset;
			IReconstructible trainFold = ((IReconstructible)folds.get(0));
			IReconstructible testFold = ((IReconstructible)folds.get(1));
			rDataset.getConstructionPlan().getInstructions().forEach(i -> {
				trainFold.addInstruction(i);
				testFold.addInstruction(i);
			});
			trainFold.addInstruction(new ReconstructionInstruction(SplitterUtil.class.getMethod("getTrainFoldOfLabelStratifiedTrainTestSplit", ILabeledDataset.class, long.class, double.class), "this", seed, relativeTrainSize));
			testFold.addInstruction(new ReconstructionInstruction(SplitterUtil.class.getMethod("getTestFoldOfLabelStratifiedTrainTestSplit", ILabeledDataset.class, long.class, double.class), "this", seed, relativeTrainSize));

			ReconstructionInstruction instruction = new ReconstructionInstruction(SplitterUtil.class.getMethod("getLabelStratifiedTrainTestSplit", ILabeledDataset.class, long.class, double.class), "this", seed, relativeTrainSize);
			return new ReproducibleSplit<>(instruction, dataset, folds.get(0), folds.get(1));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new SplitFailedException(e);
		}
	}

	public static List<ILabeledDataset<?>> getSimpleTrainTestSplit(final ILabeledDataset<?> dataset, final Random random, final double relativeTrainSize) throws SplitFailedException, InterruptedException {
		return new FilterBasedDatasetSplitter<>(new SimpleRandomSamplingFactory<>(), relativeTrainSize, random).split(dataset);
	}

	public static ILabeledDataset<?> getTrainFoldOfSimpleTrainTestSplit(final ILabeledDataset<?> dataset, final Random random, final double relativeTrainSize) throws SplitFailedException, InterruptedException{
		return getSimpleTrainTestSplit(dataset, random, relativeTrainSize).get(0);
	}

	public static ILabeledDataset<?> getTrainFoldOfSimpleTrainTestSplit(final ILabeledDataset<?> dataset, final long seed, final double relativeTrainSize) throws SplitFailedException, InterruptedException{
		return getSimpleTrainTestSplit(dataset, seed, relativeTrainSize).get(0);
	}

	public static ILabeledDataset<?> getTestFoldOfSimpleTrainTestSplit(final ILabeledDataset<?> dataset, final Random random, final double relativeTrainSize) throws SplitFailedException, InterruptedException{
		return getSimpleTrainTestSplit(dataset, random, relativeTrainSize).get(1);
	}

	public static ILabeledDataset<?> getTestFoldOfSimpleTrainTestSplit(final ILabeledDataset<?> dataset, final long seed, final double relativeTrainSize) throws SplitFailedException, InterruptedException{
		return getSimpleTrainTestSplit(dataset, seed, relativeTrainSize).get(1);
	}
}
