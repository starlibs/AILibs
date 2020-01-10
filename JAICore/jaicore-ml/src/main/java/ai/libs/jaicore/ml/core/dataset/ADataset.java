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

	private transient ILabeledInstanceSchema schema;

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

	@Override
	public Object[][] getFeatureMatrix() {
		Object[][] featureMatrix = new Object[this.size()][];
		for (int i = 0; i < this.size(); i++) {
			featureMatrix[i] = this.get(i).getAttributes();
		}
		return featureMatrix;
	}

	@Override
	public Object[] getLabelVector() {
		return this.stream().map(ILabeledInstance::getLabel).toArray();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.schema == null) ? 0 : this.schema.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		ADataset other = (ADataset) obj;
		if (this.schema == null) {
			if (other.schema != null) {
				return false;
			}
		} else if (!this.schema.equals(other.schema)) {
			return false;
		}
		return true;
	}
}
