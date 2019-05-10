package ai.libs.jaicore.ml.core.dataset.standard;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ai.libs.jaicore.ml.core.dataset.ContainsNonNumericAttributesException;
import ai.libs.jaicore.ml.core.dataset.IInstance;
import ai.libs.jaicore.ml.core.dataset.InstanceSchema;
import ai.libs.jaicore.ml.core.dataset.attribute.IAttributeValue;
import ai.libs.jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import ai.libs.jaicore.ml.core.dataset.attribute.primitive.NumericAttributeValue;
import ai.libs.jaicore.ml.core.dataset.attribute.transformer.OneHotEncodingTransformer;

public class SimpleInstance implements IInstance {
	/**
	 *
	 */
	private static final long serialVersionUID = -6945848041078727475L;

	private InstanceSchema schema;

	private final OneHotEncodingTransformer oneHotEncoder = new OneHotEncodingTransformer();

	private final ArrayList<IAttributeValue<?>> attributeValues;
	private final IAttributeValue<?> targetValue;

	public SimpleInstance(final ArrayList<IAttributeValue<?>> attributeValues, final IAttributeValue<?> targetValue) {
		this.attributeValues = attributeValues;
		this.targetValue = targetValue;
	}

	public SimpleInstance(final InstanceSchema schema, final ArrayList<IAttributeValue<?>> attributeValues, final IAttributeValue<?> targetValue) {
		this(attributeValues, targetValue);
		this.schema = schema;
	}

	@Override
	public <T> IAttributeValue<T> getAttributeValue(final int position, final Class<T> type) {
		return (IAttributeValue<T>) this.attributeValues.get(position);
	}

	@Override
	public <T> IAttributeValue<T> getTargetValue(final Class<T> type) {
		return (IAttributeValue<T>) this.targetValue;
	}

	public void setSchema(final InstanceSchema schema) {
		this.schema = schema;
	}

	@Override
	public double[] getAsDoubleVector() throws ContainsNonNumericAttributesException {
		List<Double> doubleList = new LinkedList<>();
		for (IAttributeValue<?> val : this.attributeValues) {
			if (val instanceof CategoricalAttributeValue) {
				double[] transformedValues = this.oneHotEncoder.transformAttribute(val);
				for (double doubVal : transformedValues) {
					doubleList.add(doubVal);
				}
			} else if (val instanceof NumericAttributeValue) {
				doubleList.add(((NumericAttributeValue) val).getValue());
			} else {
				throw new ContainsNonNumericAttributesException("Could not convert all the attribute values to a double vector representation.");
			}
		}
		return doubleList.stream().mapToDouble(x -> x).toArray();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (IAttributeValue<?> val : this.attributeValues) {
			sb.append(val.getValue());
			sb.append(";");
		}
		sb.append(this.targetValue.getValue());
		return sb.toString();
	}

}
