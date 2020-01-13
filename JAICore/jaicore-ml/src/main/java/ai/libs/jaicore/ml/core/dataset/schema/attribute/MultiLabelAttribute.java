package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IMultiLabelAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IMultiLabelAttributeValue;

public class MultiLabelAttribute extends ACollectionOfObjectsAttribute<String> implements IMultiLabelAttribute {

	/**
	 *
	 */
	private static final long serialVersionUID = -6840951353348119686L;
	private final List<String> domain;

	public MultiLabelAttribute(final String name, final List<String> domain) {
		super(name);
		this.domain = domain;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isValidValue(final Object value) {
		if ((value instanceof Set<?> && ((Set<?>) value).iterator().next() instanceof String)) {
			return this.domain.containsAll((Set<String>) value);
		} else if (value instanceof IMultiLabelAttributeValue) {
			return this.domain.containsAll(((IMultiLabelAttributeValue) value).getValue());
		} else {
			return false;
		}
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "MultiValuedNominalAttribute " + this.getName() + " " + this.domain;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IMultiLabelAttributeValue getAsAttributeValue(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof IMultiLabelAttributeValue) {
				return new MultiLabelAttributeValue(this, ((IMultiLabelAttributeValue) object).getValue());
			} else {
				return new MultiLabelAttributeValue(this, (Collection<String>) object);
			}
		} else {
			throw new IllegalArgumentException("No valid value for the type");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<String> getValueAsTypeInstance(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof IMultiLabelAttributeValue) {
				return ((IMultiLabelAttributeValue) object).getValue();
			} else {
				return (Collection<String>) object;
			}
		} else {
			throw new IllegalArgumentException("No valid value for the type");
		}
	}

	@Override
	public double toDouble(final Object object) {
		throw new UnsupportedOperationException("Not yet implemented in MultiValueAttribute");
	}

	@Override
	public String serializeAttributeValue(final Object value) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	public Object deserializeAttributeValue(final String string) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	public List<String> getValues() {
		return this.domain;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.domain == null) ? 0 : this.domain.hashCode());
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
		MultiLabelAttribute other = (MultiLabelAttribute) obj;
		if (this.domain == null) {
			if (other.domain != null) {
				return false;
			}
		} else if (!this.domain.equals(other.domain)) {
			return false;
		}
		return true;
	}
}
