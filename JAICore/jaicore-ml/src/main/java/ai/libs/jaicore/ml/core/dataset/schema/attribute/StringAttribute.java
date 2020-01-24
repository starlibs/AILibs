package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IStringAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IStringAttributeValue;

public class StringAttribute extends AGenericObjectAttribute<String> implements IStringAttribute {

	/**
	 *
	 */
	private static final long serialVersionUID = -4891018283425655933L;

	public StringAttribute(final String name) {
		super(name);
	}

	@Override
	public boolean isValidValue(final Object value) {
		return (value instanceof String || value instanceof IStringAttributeValue);
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[Str] " + this.getName();
	}

	@Override
	public IStringAttributeValue getAsAttributeValue(final Object object) {
		return new StringAttributeValue(this, this.getValueAsTypeInstance(object));
	}

	@Override
	protected String getValueAsTypeInstance(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof String) {
				return (String) object;
			} else {
				return ((StringAttributeValue) object).getValue();
			}
		} else {
			throw new IllegalArgumentException("No valid value of this attribute");
		}
	}

	@Override
	public double toDouble(final Object object) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public String serializeAttributeValue(final Object value) {
		return this.getValueAsTypeInstance(value);
	}

	@Override
	public Object deserializeAttributeValue(final String string) {
		return string;
	}

}
