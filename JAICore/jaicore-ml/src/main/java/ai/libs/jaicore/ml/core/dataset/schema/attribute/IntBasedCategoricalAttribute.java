package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttributeValue;

import ai.libs.jaicore.logging.ToJSONStringUtil;

public class IntBasedCategoricalAttribute extends AAttribute implements ICategoricalAttribute {

	/**
	 *
	 */
	private static final long serialVersionUID = 3727153881173459843L;

	private static final String MISSING_VALUE = "?";

	private final List<String> domain;
	private final int numCategories;

	public IntBasedCategoricalAttribute(final String name, final List<String> domain) {
		super(name);
		this.domain = domain;
		this.numCategories = domain.size();
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return this.domain.toString();
	}

	@Override
	public List<String> getLabels() {
		return this.domain;
	}

	@Override
	public boolean isBinary() {
		return this.getLabels().size() == 2;
	}

	@Override
	public boolean isValidValue(final Object attributeValue) {
		Integer value = null;
		if (attributeValue instanceof ICategoricalAttributeValue) {
			value = ((ICategoricalAttributeValue) attributeValue).getValue();
		} else if (attributeValue instanceof Integer) {
			value = (Integer) attributeValue;
		}

		if (value == null) {
			return false;
		} else {
			return value < this.domain.size();
		}
	}

	@Override
	public double encodeValue(final Object attributeValue) {
		if (!this.isValidValue(attributeValue)) {
			throw new IllegalArgumentException("No valid attribute value.");
		}
		return this.domain.indexOf(this.getLabelOfAttributeValue(attributeValue));
	}

	@Override
	public String decodeValue(final double encodedAttributeValue) {
		return this.domain.get((int) encodedAttributeValue);
	}

	private String getLabelOfAttributeValue(final Object object) {
		if (object instanceof ICategoricalAttributeValue) {
			return this.domain.get(((ICategoricalAttributeValue) object).getValue());
		} else if (object instanceof Integer) {
			return this.domain.get((int) object);
		} else if (object instanceof String) {
			return (String) object;
		} else {
			throw new IllegalArgumentException("No valid attribute value");
		}
	}

	@Override
	public ICategoricalAttributeValue getAsAttributeValue(final Object object) {
		if (this.isValidValue(object)) {
			return new IntBasedCategoricalAttributeValue(this, (int) object);
		} else {
			return null;
		}
	}

	@Override
	public ICategoricalAttributeValue getAsAttributeValue(final double encodedAttributeValue) {
		return this.getAsAttributeValue(this.decodeValue(encodedAttributeValue));
	}

	public String getNameOfCategory(final int categoryId) {
		return this.domain.get(categoryId);
	}

	public int getIdOfLabel(final String label) {
		return this.domain.indexOf(label);
	}

	@Override
	public double toDouble(final Object value) {
		if (!(value instanceof Integer)) {
			throw new IllegalArgumentException();
		}
		return this.domain.indexOf(value);
	}

	@Override
	public String serializeAttributeValue(final Object value) {
		if (value == null) {
			return MISSING_VALUE;
		}
		if (!((value instanceof Integer) || (value instanceof IntBasedCategoricalAttributeValue))) {
			throw new IllegalArgumentException("Can only serialize the integer representation of a category.");
		}
		return this.getLabelOfAttributeValue(value);
	}

	@Override
	public Integer deserializeAttributeValue(final String string) {
		String trimmedString = string.trim();
		if (string.equals(MISSING_VALUE)) {
			return null;
		}
		if ((string.startsWith("'") && string.endsWith("'")) || (string.startsWith("\"") && string.endsWith("\""))) {
			trimmedString = trimmedString.substring(1, trimmedString.length() - 1);
		}
		return this.domain.indexOf(trimmedString);
	}

	@Override
	public int getNumberOfCategories() {
		return this.numCategories;
	}

	@Override
	public String getLabelOfCategory(final Number categoryId) {
		return this.domain.get((int) categoryId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.domain == null) ? 0 : this.domain.hashCode());
		result = prime * result + this.numCategories;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		IntBasedCategoricalAttribute other = (IntBasedCategoricalAttribute) obj;
		if (this.domain == null) {
			if (other.domain != null) {
				return false;
			}
		} else if (!this.domain.equals(other.domain)) {
			return false;
		}
		return this.numCategories == other.numCategories;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("domain", this.domain);
		fields.put("numCategories", this.numCategories);
		return ToJSONStringUtil.toJSONString(IntBasedCategoricalAttribute.class.getName(), fields);
	}
}
