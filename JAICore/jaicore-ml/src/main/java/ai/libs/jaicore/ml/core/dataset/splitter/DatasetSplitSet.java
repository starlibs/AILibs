package ai.libs.jaicore.ml.core.dataset.splitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.api4.java.ai.ml.classification.execution.IDatasetSplitSet;
import org.api4.java.ai.ml.core.dataset.IDataset;

public class DatasetSplitSet<D extends IDataset<?>> implements IDatasetSplitSet<D> {

	private final List<List<D>> splits = new ArrayList<>();

	public DatasetSplitSet() {
		/* do nothing */
	}

	public DatasetSplitSet(final IDatasetSplitSet<D> set) {
		int n = set.getNumberOfSplits();
		for (int i = 0; i < n; i++) {
			this.splits.add(new ArrayList<>(set.getFolds(i)));
		}
	}

	public DatasetSplitSet(final List<List<D>> splits) {
		this.splits.addAll(splits);
	}

	public void addSplit(final List<D> split) {
		this.splits.add(split);
	}

	@Override
	public int getNumberOfSplits() {
		return this.splits.size();
	}

	@Override
	public int getNumberOfFoldsPerSplit() {
		return this.splits.get(0).size();
	}

	public int getNumberOfFoldsForSplit(final int pos) {
		return this.splits.get(pos).size();
	}

	@Override
	public List<D> getFolds(final int splitId) {
		return Collections.unmodifiableList(this.splits.get(splitId));
	}

}