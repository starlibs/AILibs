package ai.libs.jaicore.ml.core.evaluation.splitsetgenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.splitter.IRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.IDatasetSplitSet;

public class CachingMonteCarloCrossValidationSplitSetGenerator<D extends ILabeledDataset<?>> extends MonteCarloCrossValidationSplitSetGenerator<D> {

	private Map<Integer, IDatasetSplitSet<D>> cache = new HashMap<>();
	
	public CachingMonteCarloCrossValidationSplitSetGenerator(IRandomDatasetSplitter<D> datasetSplitter, int repeats,
			Random random) {
		super(datasetSplitter, repeats, random);
	}

	@Override
	public IDatasetSplitSet<D> nextSplitSet(final D data) throws InterruptedException, SplitFailedException {
		int hashCode = data.hashCode();
		if(!cache.containsKey(hashCode)) {
			this.cache.put(hashCode, super.nextSplitSet(data));
		}
		return this.cache.get(hashCode);
	}
}
