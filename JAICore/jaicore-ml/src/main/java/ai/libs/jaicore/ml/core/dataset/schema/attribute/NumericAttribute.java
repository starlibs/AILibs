package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttributeValue;

public class NumericAttribute extends AAttribute implements INumericAttribute {

	/**
	 *
	 */
	private static final long serialVersionUID = 657993241775006166L;

	public NumericAttribute(final String name) {
		super(name);
	}

	@Override
	public boolean isValidValue(final Object value) {
		if (value instanceof Number || value instanceof INumericAttributeValue) {
			return true;
		}
		return false;
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[Num] " + this.getName();
	}

	private double getAttributeValueAsDouble(final Object attributeValue) {
		if (attributeValue instanceof INumericAttributeValue) {
			return ((INumericAttributeValue) attributeValue).getValue();
		} else if (attributeValue instanceof Integer) {
			return ((Integer) attributeValue) * 1.0;
		} else if (attributeValue instanceof Long) {
			return ((Long) attributeValue) * 1.0;
		} else if (attributeValue instanceof Double) {
			return (Double) attributeValue;
		} else {
			throw new IllegalArgumentException("No valid attribute value");
		}
	}

	@Override
	public INumericAttributeValue getAsAttributeValue(final Object obj) {
		return new NumericAttributeValue(this, this.getAttributeValueAsDouble(obj));
	}

	@Override
	public double encodeValue(final Object attributeValue) {
		if (!this.isValidValue(attributeValue)) {
			throw new IllegalArgumentException("No valid attribute value");
		}
		return this.getAttributeValueAsDouble(attributeValue);
	}

	@Override
	public Double decodeValue(final double encodedAttributeValue) {
		return encodedAttributeValue;
	}

	@Override
	public IAttributeValue getAsAttributeValue(final double encodedAttributeValue) {
		return new NumericAttributeValue(this, encodedAttributeValue);
	}

	@Override
	public double toDouble(final Object object) {
		return this.getAttributeValueAsDouble(object);
	}

	@Override
	public String serializeAttributeValue(final Object value) {
		return this.getAttributeValueAsDouble(value) + "";
	}

	@Override
	public Object deserializeAttributeValue(final String string) {
		return Double.parseDouble(string);
	}

}
