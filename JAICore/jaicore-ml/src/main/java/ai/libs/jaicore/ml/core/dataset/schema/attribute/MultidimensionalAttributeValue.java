package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Arrays;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;

public class MultidimensionalAttributeValue implements IAttributeValue {

	private final double[][] value;
	private final MultidimensionalAttribute attribute;

	public MultidimensionalAttributeValue(final MultidimensionalAttribute attribute, final double[][] value) {
		this.value = value;
		this.attribute = attribute;
	}

	@Override
	public IAttribute getAttribute() {
		return this.attribute;
	}

	@Override
	public double[][] getValue() {
		return this.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.attribute == null) ? 0 : this.attribute.hashCode());
		result = prime * result + Arrays.deepHashCode(this.value);
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
		MultidimensionalAttributeValue other = (MultidimensionalAttributeValue) obj;
		if (this.attribute == null) {
			if (other.attribute != null) {
				return false;
			}
		} else if (!this.attribute.equals(other.attribute)) {
			return false;
		}
		if (!Arrays.deepEquals(this.value, other.value)) {
			return false;
		}
		return true;
	}

}
