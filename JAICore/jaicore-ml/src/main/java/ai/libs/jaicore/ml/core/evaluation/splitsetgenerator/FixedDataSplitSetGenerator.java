package ai.libs.jaicore.ml.core.evaluation.splitsetgenerator;

import org.api4.java.ai.ml.classification.execution.IDatasetSplitSet;
import org.api4.java.ai.ml.classification.execution.IDatasetSplitSetGenerator;
import org.api4.java.ai.ml.classification.execution.IFixedDatasetSplitSetGenerator;
import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;

/**
 * This is an IDatasetSplitSetGenerator that produced splits for only one initially given dataset.
 *
 * It can be used as a split generator in a context where the data are not passed along the usage of the splitter.
 *
 * @author felix
 *
 * @param <D>
 */
public class FixedDataSplitSetGenerator<D extends IDataset<?>> implements IFixedDatasetSplitSetGenerator<D> {

	private final D data;
	private final IDatasetSplitSetGenerator<D> generator;

	public FixedDataSplitSetGenerator(final D data, final IDatasetSplitSetGenerator<D> generator) {
		super();
		this.data = data;
		this.generator = generator;
	}

	@Override
	public int getNumSplitsPerSet() {
		return this.generator.getNumFoldsPerSplit();
	}

	@Override
	public int getNumFoldsPerSplit() {
		return this.generator.getNumFoldsPerSplit();
	}

	@Override
	public IDatasetSplitSet<D> nextSplitSet() throws InterruptedException, SplitFailedException {
		return this.generator.nextSplitSet(this.data);
	}

	@Override
	public D getDataset() {
		return this.data;
	}

	@Override
	public String toString() {
		return "FixedDataSplitSetGenerator [data=" + this.data + ", generator=" + this.generator + "]";
	}
}
