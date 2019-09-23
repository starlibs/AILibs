package ai.libs.jaicore.ml.core.tabular.dataset.attribute;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttributeValue;

public class CategoricalAttribute extends AAttribute implements ICategoricalAttribute {

	/**
	 *
	 */
	private static final long serialVersionUID = 3727153881173459843L;

	private final List<String> domain;

	public CategoricalAttribute(final String name, final List<String> domain) {
		super(name);
		this.domain = domain;
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[Nom] " + this.getName() + " " + this.domain;
	}

	@Override
	public List<String> getValues() {
		return this.domain;
	}

	@Override
	public boolean isBinary() {
		return this.getValues().size() == 2;
	}

	@Override
	public boolean isValidValue(final Object attributeValue) {
		String value = null;
		if (attributeValue instanceof ICategoricalAttributeValue) {
			value = ((ICategoricalAttributeValue) attributeValue).getValue();
		} else if (attributeValue instanceof String) {
			value = (String) attributeValue;
		}

		if (value == null) {
			return false;
		} else {
			return this.domain.contains(value);
		}
	}

	@Override
	public double encodeValue(final Object attributeValue) {
		if (!this.isValidValue(attributeValue)) {
			throw new IllegalArgumentException("No valid attribute value.");
		}
		return this.domain.indexOf(this.getAttributeValueAsString(attributeValue)) + 1;
	}

	@Override
	public String decodeValue(final double encodedAttributeValue) {
		return this.domain.get((int) encodedAttributeValue - 1);
	}

	private String getAttributeValueAsString(final Object object) {
		if (object instanceof ICategoricalAttributeValue) {
			return ((ICategoricalAttributeValue) object).getValue();
		} else if (object instanceof String) {
			return (String) object;
		} else {
			throw new IllegalArgumentException("No valid attribute value");
		}
	}

	@Override
	public ICategoricalAttributeValue getAsAttributeValue(final Object object) {
		if (this.isValidValue(object)) {
			return new CategoricalAttributeValue(this, this.getAttributeValueAsString(object));
		} else {
			return null;
		}
	}

	@Override
	public ICategoricalAttributeValue getAsAttributeValue(final double encodedAttributeValue) {
		return this.getAsAttributeValue(this.decodeValue(encodedAttributeValue));
	}

}
