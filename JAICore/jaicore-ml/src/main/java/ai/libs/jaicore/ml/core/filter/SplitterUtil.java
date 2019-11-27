package ai.libs.jaicore.ml.core.filter;

import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;

public class SplitterUtil {
	public static List<ILabeledDataset<?>> getLabelStratifiedTrainTestSplit(final ILabeledDataset<?> dataset, final Random random, final double relativeTrainSize) throws SplitFailedException, InterruptedException {
		return new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>(), relativeTrainSize, random).split(dataset);
	}
}
