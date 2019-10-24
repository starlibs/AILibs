package ai.libs.jaicore.ml.core.dataset;

import java.util.ArrayList;
import java.util.Optional;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public abstract class ADataset<I extends ILabeledInstance> extends ArrayList<I> implements ILabeledDataset<I> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1158266286156653852L;

	private ILabeledInstanceSchema schema;

	protected ADataset(final ILabeledInstanceSchema schema) {
		super();
		this.schema = schema;
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		return this.schema;
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
