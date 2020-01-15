package ai.libs.jaicore.ml.core.dataset.clusterable;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

import ai.libs.jaicore.ml.core.dataset.ADataset;
import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;

public class ClusterableDataset extends ADataset<IClusterableInstance> implements ILabeledDataset<IClusterableInstance> {

	/**
	 *
	 */
	private static final long serialVersionUID = -6066251665166527020L;

	public ClusterableDataset(final ILabeledInstanceSchema schema) {
		super(schema);
	}

	public ClusterableDataset(final ILabeledDataset<ILabeledInstance> dataset) {
		this(dataset.getInstanceSchema());
		dataset.stream().map(x -> (IClusterableInstance) x).forEach(this::add);
	}

	@Override
	public void removeColumn(final int columnPos) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	public ClusterableDataset createEmptyCopy() throws DatasetCreationException, InterruptedException {
		return new ClusterableDataset(this.getInstanceSchema());
	}

	@Override
	public ClusterableDataset createCopy() throws DatasetCreationException, InterruptedException {
		ClusterableDataset copy = this.createEmptyCopy();
		for (IClusterableInstance i : this) {
			copy.add(i);
		}
		return copy;
	}

}
