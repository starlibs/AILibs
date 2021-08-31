package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttributeValue;

public class IntBasedCategoricalAttribute extends AAttribute implements ICategoricalAttribute {

	/**
	 *
	 */
	private static final long serialVersionUID = 3727153881173459843L;

	private final Map<String, Integer> valuePosCache = new HashMap<>();
	private final List<String> domain;
	private final int numCategories;

	public IntBasedCategoricalAttribute(final String name, final List<String> domain) {
		super(name);
		this.domain = domain;
		IntStream.range(0, domain.size()).forEach(i -> this.valuePosCache.put(domain.get(i), i));
		this.numCategories = domain.size();
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return this.domain.toString();
	}

	@Override
	public List<String> getLabels() {
		return Collections.unmodifiableList(this.domain);
	}

	@Override
	public boolean isBinary() {
		return this.getLabels().size() == 2;
	}

	@Override
	public boolean isValidValue(final Object attributeValue) {
		if (attributeValue == null) {
			return true;
		}
		Integer value = null;
		if (attributeValue instanceof ICategoricalAttributeValue) {
			value = ((ICategoricalAttributeValue) attributeValue).getValue();
		} else if (attributeValue instanceof Integer) {
			value = (Integer) attributeValue;
		} else if (attributeValue instanceof Double) {
			value = (int) (double) attributeValue;
		} else if (this.domain.contains(attributeValue)) {
			value = this.valuePosCache.get(attributeValue);
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
		return this.valuePosCache.get(this.getLabelOfAttributeValue(attributeValue));
	}

	@Override
	public String decodeValue(final double encodedAttributeValue) {
		return this.domain.get((int) encodedAttributeValue);
	}

	private String getLabelOfAttributeValue(final Object object) {
		if (object instanceof ICategoricalAttributeValue) {
			return this.domain.get(((ICategoricalAttributeValue) object).getValue());
		} else if (object instanceof String) {
			return (String) object;
		} else {
			throw new IllegalArgumentException("No valid attribute value");
		}
	}

	@Override
	public ICategoricalAttributeValue getAsAttributeValue(final Object object) {
		if (object == null) {
			return null;
		}
		if (this.isValidValue(object)) {
			int cObject;
			if (object instanceof Integer) {
				cObject = (int) object;
			}
			else if (object instanceof ICategoricalAttributeValue) {
				cObject = ((ICategoricalAttributeValue) object).getValue();
			}
			else {
				cObject = this.valuePosCache.get(object);
				if (cObject < 0) {
					throw new IllegalStateException("Object should be parseable after the test.");
				}
			}
			return new IntBasedCategoricalAttributeValue(this, cObject);
		} else {
			throw new IllegalArgumentException(object + " is an invalid value for categorical attribute with domain " + this.getStringDescriptionOfDomain());
		}
	}

	@Override
	public ICategoricalAttributeValue getAsAttributeValue(final double encodedAttributeValue) {
		return this.getAsAttributeValue(this.decodeValue(encodedAttributeValue));
	}

	public String getNameOfCategory(final int categoryId) {
		return this.domain.get(categoryId);
	}

	@Override
	public double toDouble(final Object value) {
		if (!(value instanceof Integer)) {
			throw new IllegalArgumentException();
		}
		return (double)value;
	}

	@Override
	public String serializeAttributeValue(final Object value) {
		if (value == null) {
			return null;
		}
		if (!(value instanceof Integer)) {
			if (!this.domain.contains(value)) {
				throw new IllegalArgumentException("The given value \"" + value + "\" is not part of the domain and cannot be serialized. The domain is: " + this.domain);
			}
			return (String) value; // here we know that this must be a String; otherwise it could not be in the domain!
		}
		if (((Integer) value) < 0) {
			return null;
		}
		return this.domain.get((Integer) value);
	}

	@Override
	public Integer deserializeAttributeValue(final String string) {
		String trimmedString = string.trim();
		if ((string.startsWith("'") && string.endsWith("'")) || (string.startsWith("\"") && string.endsWith("\""))) {
			trimmedString = trimmedString.substring(1, trimmedString.length() - 1);
		}
		int indexOf = this.valuePosCache.get(trimmedString); // storing this as an int here is important in order to obtain an exception on null
		return indexOf;
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
}
