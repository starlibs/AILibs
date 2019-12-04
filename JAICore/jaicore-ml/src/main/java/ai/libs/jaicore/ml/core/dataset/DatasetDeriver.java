package ai.libs.jaicore.ml.core.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

public class DatasetDeriver<D extends IDataset<?>> {

	private class GenericCapsula<I extends IInstance> {
		final IDataset<I> dsOriginal;
		final int initialDatasetHashCode; // this is for consistency checks
		final List<Integer> indicesToCopy = new ArrayList<>();
		final Class<I> classOfInstances;

		public GenericCapsula(final IDataset<I> dataset, final Class<I> clazz) {
			super();
			this.dsOriginal = dataset;
			this.initialDatasetHashCode = dataset.hashCode();
			this.classOfInstances = clazz;
		}

		public IDataset<I> getCopyBasedOnDefinedLines() throws InterruptedException, DatasetCreationException {
			if (this.initialDatasetHashCode != this.dsOriginal.hashCode()) {
				throw new IllegalStateException("Dataset underlying the deriver has changed!");
			}
			IDataset<I> copy = this.dsOriginal.createEmptyCopy();
			for (int index : this.indicesToCopy) {
				copy.add(this.dsOriginal.get(index));
			}
			return copy;
		}

		//		public void addInstance(final IInstance inst) {
		//			if (!this.dsOriginal.contains(inst)) {
		//				throw new IllegalArgumentException("The instance " + inst + " is not contained in the given dataset of type " + this.dsOriginal.getClass().getName() + " with " + this.dsOriginal.size() + " elements.");
		//			}
		//			this.itemsToCopy.add((I)inst);
		//		}

		public void addInstance(final int inst) {
			this.indicesToCopy.add(inst);
		}
	}

	private final GenericCapsula<?> caps;

	public DatasetDeriver(final D dataset) {
		this(dataset, (Class<? extends IInstance>) dataset.get(0).getClass());
	}

	protected <I extends IInstance> DatasetDeriver(final D dataset, final Class<I> instanceClass) {
		this.caps = new GenericCapsula<>((IDataset<I>) dataset, instanceClass);
	}

	//	public void add(final IInstance inst) {
	//		this.caps.addInstance(inst);
	//	}

	public void add(final int item) {
		this.caps.addInstance(item);
	}

	//	public void addAll(final Collection<? extends IInstance> instances) {
	//		for (IInstance i : instances) {
	//			this.add(i);
	//		}
	//	}

	public void addIndices(final Collection<Integer> indices) {
		for (int i : indices) {
			this.caps.addInstance(i);
		}
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
		return Collections.unmodifiableList(indicesInBuiltDataset.stream().map(i -> this.caps.indicesToCopy.get(i)).collect(Collectors.toList()));
	}

	public List<Integer> getIndicesOfNewInstancesInOriginalDataset(final List<Integer> indicesInBuiltDataset) {
		return Collections.unmodifiableList(indicesInBuiltDataset.stream().map(i -> this.caps.indicesToCopy.get(i)).collect(Collectors.toList()));
	}
}
