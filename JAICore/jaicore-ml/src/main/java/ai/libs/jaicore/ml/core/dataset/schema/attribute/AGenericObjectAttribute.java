package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;

/**
 *
 * @author mwever
 *
 * @param <O> The type of object that can be stored.
 */
public abstract class AGenericObjectAttribute<O> extends AAttribute {

	/**
	 *
	 */
	private static final long serialVersionUID = 614829108498630281L;

	private transient Map<O, Double> objectToDoubleMap = new HashMap<>();
	private transient Map<Double, O> doubleToObjectMap = new HashMap<>();
	private AtomicInteger objectCounter = new AtomicInteger(1);

	protected AGenericObjectAttribute(final String name) {
		super(name);
	}

	@Override
	public double encodeValue(final Object attributeValue) {
		if (!this.isValidValue(attributeValue)) {
			throw new IllegalArgumentException("No valid attribute value");
		}
		O value = this.getValueAsTypeInstance(attributeValue);
		if (!this.objectToDoubleMap.containsKey(attributeValue)) {
			double encodedValue = this.objectCounter.getAndIncrement();
			this.objectToDoubleMap.put(value, encodedValue);
			this.doubleToObjectMap.put(encodedValue, value);
			return encodedValue;
		} else {
			return this.objectToDoubleMap.get(value);
		}
	}

	@Override
	public O decodeValue(final double encodedAttributeValue) {
		return this.doubleToObjectMap.get(encodedAttributeValue);
	}

	@Override
	public IAttributeValue getAsAttributeValue(final double encodedAttributeValue) {
		return this.getAsAttributeValue(this.doubleToObjectMap.get(encodedAttributeValue));
	}

	protected abstract O getValueAsTypeInstance(Object object);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.doubleToObjectMap == null) ? 0 : this.doubleToObjectMap.hashCode());
		result = prime * result + ((this.objectCounter == null) ? 0 : this.objectCounter.hashCode());
		result = prime * result + ((this.objectToDoubleMap == null) ? 0 : this.objectToDoubleMap.hashCode());
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
		AGenericObjectAttribute other = (AGenericObjectAttribute) obj;
		if (this.doubleToObjectMap == null) {
			if (other.doubleToObjectMap != null) {
				return false;
			}
		} else if (!this.doubleToObjectMap.equals(other.doubleToObjectMap)) {
			return false;
		}
		if (this.objectCounter == null) {
			if (other.objectCounter != null) {
				return false;
			}
		} else if (this.objectCounter.get() != other.objectCounter.get()) {
			return false;
		}
		if (this.objectToDoubleMap == null) {
			if (other.objectToDoubleMap != null) {
				return false;
			}
		} else if (!this.objectToDoubleMap.equals(other.objectToDoubleMap)) {
			return false;
		}
		return true;
	}
}
