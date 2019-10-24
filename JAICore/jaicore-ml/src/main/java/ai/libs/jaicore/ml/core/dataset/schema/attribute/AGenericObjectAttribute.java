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

	private Map<O, Double> objectToDoubleMap = new HashMap<>();
	private Map<Double, O> doubleToObjectMap = new HashMap<>();
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

}
