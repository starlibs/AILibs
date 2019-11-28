package ai.libs.jaicore.ml.classification.singlelabel.dataset;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationDataset;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;
import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

import ai.libs.jaicore.ml.core.dataset.ADataset;

public class SingleLabelClassificationDataset extends ADataset<ISingleLabelClassificationInstance> implements ISingleLabelClassificationDataset {

	public SingleLabelClassificationDataset(final ILabeledInstanceSchema schema) {
		super(schema);
	}

	public SingleLabelClassificationDataset(final ILabeledDataset<? extends ILabeledInstance> dataset) {
		super(dataset.getInstanceSchema());
		dataset.stream().map(x -> new SingleLabelClassificationInstance(x)).forEach(this::add);
	}

	@Override
	public ILabeledDataset<ISingleLabelClassificationInstance> createEmptyCopy() throws DatasetCreationException, InterruptedException {
		return new SingleLabelClassificationDataset(this.getInstanceSchema());
	}

	@Override
	public IDataset<ISingleLabelClassificationInstance> createCopy() throws DatasetCreationException, InterruptedException {
		return new SingleLabelClassificationDataset(this);
	}

	@Override
	public void removeColumn(final int columnPos) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}
}
