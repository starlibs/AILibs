package ai.libs.jaicore.ml.core.newdataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

public class Dataset extends ArrayList<IInstance> implements ILabeledDataset<IInstance> {

	/**
	 *
	 */
	private static final long serialVersionUID = -3643080541896274181L;

	private final ILabeledInstanceSchema schema;

	public Dataset(final ILabeledInstanceSchema schema) {
		this.schema = schema;
	}

	public Dataset(final ILabeledInstanceSchema schema, final List<IInstance> instances) {
		this(schema);
		this.addAll(instances);
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		return this.schema;
	}

	@Override
	public IDataset<IInstance> createEmptyCopy() throws DatasetCreationException, InterruptedException {
		return new Dataset(this.schema);
	}

	@Override
	public Object[][] getFeatureMatrix() {
		return (Object[][]) IntStream.range(0, this.size()).mapToObj(x -> this.get(x).getAttributes()).toArray();
	}

	@Override
	public Object[] getLabelVector() {
		return IntStream.range(0, this.size()).mapToObj(x -> this.get(x).getLabel()).toArray();
	}

	@Override
	public void removeColumn(final int columnPos) {
		this.schema.removeAttribute(columnPos);
		this.stream().forEach(x -> x.removeColumn(columnPos));
	}

	@Override
	public void removeColumn(final String columnName) {
		Optional<IAttribute> att = this.schema.getAttributeList().stream().filter(x -> x.getName().equals(columnName)).findFirst();
		if (att.isPresent()) {
			this.removeColumn(this.schema.getAttributeList().indexOf(att.get()));
		} else {
			throw new IllegalArgumentException("There is no such attribute with name " + columnName + " to remove.");
		}
	}

	@Override
	public void removeColumn(final IAttribute attribute) {
		int index = this.schema.getAttributeList().indexOf(attribute);
		if (index >= 0) {
			this.removeColumn(index);
		} else {
			throw new IllegalArgumentException("There is no such attribute with name " + attribute.getName() + " to remove.");
		}
	}

}
