package jaicore.ml.core.dataset.standard;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jaicore.ml.core.dataset.ContainsNonNumericAttributesException;
import jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.InstanceSchema;
import jaicore.ml.core.dataset.attribute.IAttributeType;

public class SimpleDataset<L> extends LinkedList<SimpleInstance<L>> implements IOrderedLabeledAttributeArrayDataset<SimpleInstance<L>, L> {

	/**
	 *
	 */
	private static final long serialVersionUID = -404523661106060818L;

	private final InstanceSchema<L> instanceSchema;

	public SimpleDataset(final InstanceSchema<L> instanceSchema) {
		this.instanceSchema = instanceSchema;
	}

	@Override
	public IAttributeType<L> getTargetType() {
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
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.instanceSchema.toString());

		sb.append("\n");
		sb.append("%instances");
		sb.append("\n");
		for (SimpleInstance<L> inst : this) {
			sb.append(inst);
			sb.append("\n");
		}
		return sb.toString();
	}

	public String printDoubleRepresentation() throws ContainsNonNumericAttributesException {
		StringBuilder sb = new StringBuilder();

		for (SimpleInstance<L> inst : this) {
			sb.append(Arrays.toString(inst.getAsDoubleVector()));
			sb.append("\n");
		}

		return sb.toString();
	}

	@Override
	public SimpleDataset<L> createEmpty() {
		return new SimpleDataset<>(this.instanceSchema);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.instanceSchema == null) ? 0 : this.instanceSchema.hashCode());
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
		SimpleDataset<L> other = (SimpleDataset<L>) obj;
		if (this.instanceSchema == null) {
			if (other.instanceSchema != null) {
				return false;
			}
		} else if (!this.instanceSchema.equals(other.instanceSchema)) {
			return false;
		}
		return true;
	}
}
