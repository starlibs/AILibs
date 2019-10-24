package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Collection;
import java.util.Set;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IMultiLabelAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IMultiLabelAttributeValue;

public class MultiValueAttribute extends ACollectionOfObjectsAttribute<String> implements IMultiLabelAttribute {

	/**
	 *
	 */
	private static final long serialVersionUID = -6840951353348119686L;
	private final Collection<String> domain;

	public MultiValueAttribute(final String name, final Collection<String> domain) {
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
				return new MultiValueAttributeValue(this, ((IMultiLabelAttributeValue) object).getValue());
			} else {
				return new MultiValueAttributeValue(this, (Collection<String>) object);
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
		throw new UnsupportedOperationException("Not yet implemented.");// TODO
	}

	@Override
	public Object deserializeAttributeValue(final String string) {
		throw new UnsupportedOperationException("Not yet implemented.");// TODO
	}

}
