package ai.libs.jaicore.ml.core.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

public class DatasetDeriver<D extends IDataset<?>> {

	private class GenericCapsula<I extends IInstance> {
		final IDataset<I> dsOriginal;
		final List<I> itemsToCopy = new ArrayList<>();
		final Class<I> classOfInstances;

		public GenericCapsula(final IDataset<I> dataset, final Class<I> clazz) {
			super();
			this.dsOriginal = dataset;
			this.classOfInstances = clazz;
		}

		public IDataset<I> getCopyBasedOnDefinedLines() throws InterruptedException, DatasetCreationException {
			IDataset<I> copy = this.dsOriginal.createEmptyCopy();
			for (I item : this.itemsToCopy) {
				copy.add(item);
			}
			return copy;
		}

		public void addInstance(final IInstance inst) {
			if (!this.dsOriginal.contains(inst)) {
				throw new IllegalArgumentException();
			}
			this.itemsToCopy.add((I)inst);
		}

		public void addInstance(final int inst) {
			this.itemsToCopy.add(this.dsOriginal.get(inst));
		}
	}

	private final GenericCapsula<?> caps;

	public DatasetDeriver(final D dataset) {
		this(dataset, (Class<? extends IInstance>) dataset.get(0).getClass());
	}

	protected <I extends IInstance> DatasetDeriver(final D dataset, final Class<I> instanceClass) {
		this.caps = new GenericCapsula<>((IDataset<I>) dataset, instanceClass);
	}

	public void add(final IInstance inst) {
		this.caps.addInstance(inst);
	}

	public void add(final int item) {
		this.caps.addInstance(item);
	}

	public void addAll(final Collection<? extends IInstance> instances) {
		for (IInstance i : instances) {
			this.add(i);
		}
	}

	public D build() throws InterruptedException, DatasetCreationException {
		return (D)this.caps.getCopyBasedOnDefinedLines();
	}

	public int currentSizeOfTarget() {
		return this.caps.itemsToCopy.size();
	}
}
