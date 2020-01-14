package ai.libs.jaicore.ml.core.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

public class DatasetDeriver<D extends IDataset<?>> {

	private class GenericCapsula<I extends IInstance> {
		private final IDataset<I> dsOriginal;
		private final int initialDatasetHashCode; // this is for consistency checks
		private final List<Integer> indicesToCopy = new ArrayList<>();

		public GenericCapsula(final IDataset<I> dataset, final Class<I> clazz) {
			super();
			this.dsOriginal = dataset;
			this.initialDatasetHashCode = dataset.hashCode();
			if (!clazz.isInstance(dataset.get(0))) {
				throw new IllegalArgumentException();
			}
		}

		public IDataset<I> getCopyBasedOnDefinedLines() throws InterruptedException, DatasetCreationException {
			if (this.initialDatasetHashCode != this.dsOriginal.hashCode()) {
				throw new IllegalStateException("Dataset underlying the deriver has changed!");
			}
			IDataset<I> copy = this.dsOriginal.createEmptyCopy();
			for (int index : this.indicesToCopy) {
				copy.add(this.dsOriginal.get(index));
			}
			if (copy.size() != this.indicesToCopy.size()) {
				throw new IllegalStateException("The copy has " + copy.size() + " elements while it should have " + this.indicesToCopy.size() + ".");
			}
			return copy;
		}

		public void addInstance(final int inst, final int count) {
			for (int i = 0; i < count; i++) {
				this.indicesToCopy.add(inst);
			}
		}
	}

	private final GenericCapsula<?> caps;

	public DatasetDeriver(final D dataset) {
		this(dataset, (Class<? extends IInstance>) dataset.get(0).getClass());
	}

	protected <I extends IInstance> DatasetDeriver(final D dataset, final Class<I> instanceClass) {
		this.caps = new GenericCapsula<>((IDataset<I>) dataset, instanceClass);
	}

	public void add(final int item, final int count) {
		this.caps.addInstance(item, count);
	}

	public void add(final int item) {
		this.add(item, 1);
	}

	public void addIndices(final Collection<Integer> indices, final int count) {
		Objects.requireNonNull(indices);
		for (int i : indices) {
			this.caps.addInstance(i, count);
		}
	}

	public void addIndices(final Collection<Integer> indices) {
		this.addIndices(indices, 1);
	}

	public boolean contains(final IInstance inst) {
		return this.caps.indicesToCopy.contains(this.caps.dsOriginal.indexOf(inst));
	}

	public D build() throws InterruptedException, DatasetCreationException {
		return (D)this.caps.getCopyBasedOnDefinedLines();
	}

	public int currentSizeOfTarget() {
		return this.caps.indicesToCopy.size();
	}

	public D getDataset() {
		return (D)this.caps.dsOriginal;
	}

	public List<Integer> getIndicesOfNewInstancesInOriginalDataset() {
		return Collections.unmodifiableList(this.caps.indicesToCopy);
	}

	public Collection<Integer> getIndicesOfNewInstancesInOriginalDataset(final Collection<Integer> indicesInBuiltDataset) {
		return Collections.unmodifiableList(indicesInBuiltDataset.stream().map(this.caps.indicesToCopy::get).collect(Collectors.toList()));
	}

	public List<Integer> getIndicesOfNewInstancesInOriginalDataset(final List<Integer> indicesInBuiltDataset) {
		return Collections.unmodifiableList(indicesInBuiltDataset.stream().map(this.caps.indicesToCopy::get).collect(Collectors.toList()));
	}
}
