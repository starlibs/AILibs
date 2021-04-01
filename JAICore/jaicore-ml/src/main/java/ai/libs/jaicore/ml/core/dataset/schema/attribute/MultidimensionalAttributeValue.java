package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;

public class MultidimensionalAttributeValue<O> implements IAttributeValue {
	protected O value;
	protected MultidimensionalAttribute<O> attribute;

	public MultidimensionalAttributeValue(final MultidimensionalAttribute<O> attribute, final O object) {
		this.value = object;
		this.attribute = attribute;
	}

	@Override
	public IAttribute getAttribute() {
		return this.attribute;
	}

	@Override
	public O getValue() {
		return this.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.attribute == null) ? 0 : this.attribute.hashCode());
		result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
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
		MultidimensionalAttributeValue<O> other = (MultidimensionalAttributeValue<O>) obj;
		if (this.attribute == null) {
			if (other.attribute != null) {
				return false;
			}
		} else if (!this.attribute.equals(other.attribute)) {
			return false;
		}
		if (this.value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!this.value.equals(other.value)) {
			return false;
		}
		return true;
	}

}
