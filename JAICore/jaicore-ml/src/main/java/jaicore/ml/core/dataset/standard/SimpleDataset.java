package jaicore.ml.core.dataset.standard;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jaicore.ml.core.dataset.ContainsNonNumericAttributesException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.InstanceSchema;
import jaicore.ml.core.dataset.attribute.IAttributeType;

public class SimpleDataset extends LinkedList<SimpleInstance> implements IDataset<SimpleInstance> {

	/**
	 *
	 */
	private static final long serialVersionUID = -404523661106060818L;

	private final InstanceSchema instanceSchema;

	public SimpleDataset(final InstanceSchema instanceSchema) {
		this.instanceSchema = instanceSchema;
	}

	@Override
	public <T> IAttributeType<T> getTargetType(final Class<T> clazz) {
		return this.instanceSchema.getTargetType(clazz);
	}

	@Override
	public IAttributeType<?> getTargetType() {
		return this.instanceSchema.getTargetType();
	}

	@Override
	public List<IAttributeType<?>> getAttributeTypes() {
		return this.instanceSchema.getAttributeTypeList();
	}

	@Override
	public int getNumberOfAttributes() {
		return this.instanceSchema.getAttributeTypeList().size();
	}

	@Override
	public boolean add(final SimpleInstance instance) {
		instance.setSchema(this.instanceSchema);
		return super.add(instance);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.instanceSchema.toString());

		sb.append("\n");
		sb.append("%instances");
		sb.append("\n");
		for (SimpleInstance inst : this) {
			sb.append(inst);
			sb.append("\n");
		}
		return sb.toString();
	}

	public String printDoubleRepresentation() throws ContainsNonNumericAttributesException {
		StringBuilder sb = new StringBuilder();

		for (SimpleInstance inst : this) {
			sb.append(Arrays.toString(inst.getAsDoubleVector()));
			sb.append("\n");
		}

		return sb.toString();
	}
	
	@Override
	public SimpleDataset createEmpty() {
		return new SimpleDataset(instanceSchema);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((instanceSchema == null) ? 0 : instanceSchema.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleDataset other = (SimpleDataset) obj;
		if (instanceSchema == null) {
			if (other.instanceSchema != null)
				return false;
		} else if (!instanceSchema.equals(other.instanceSchema))
			return false;
		return true;
	}
}
