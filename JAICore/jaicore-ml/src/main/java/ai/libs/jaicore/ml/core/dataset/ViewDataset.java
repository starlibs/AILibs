package ai.libs.jaicore.ml.core.dataset;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;

public class ViewDataset extends ADataset<ILabeledInstance> {


	public ViewDataset(final ILabeledDataset<? extends ILabeledInstance> baseDataset, final Collection<Integer> indices) {
		super();
		ILabeledInstanceSchema schema = baseDataset.getInstanceSchema();
		List<IAttribute> atts = indices.stream().map(i -> schema.getAttribute(i)).collect(Collectors.toList());
		this.setInstanceScehma(new LabeledInstanceSchema(schema.getRelationName(), atts, schema.getLabelAttribute()));
		baseDataset.forEach(i -> this.add(new ViewInstance(i, indices)));
	}

	@Override
	public ILabeledDataset<ILabeledInstance> createEmptyCopy() throws DatasetCreationException, InterruptedException {
		return new Dataset(this.getInstanceSchema());
	}

	@Override
	public ILabeledDataset<ILabeledInstance> createCopy() throws DatasetCreationException, InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeColumn(final int columnPos) {
		throw new UnsupportedOperationException();
	}
}
