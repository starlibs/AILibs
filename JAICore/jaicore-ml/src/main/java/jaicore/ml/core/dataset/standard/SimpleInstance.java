package jaicore.ml.core.dataset.standard;

import java.util.LinkedList;
import java.util.List;

import jaicore.ml.core.dataset.ContainsNonNumericAttributesException;
import jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeValue;
import jaicore.ml.core.dataset.attribute.transformer.OneHotEncodingTransformer;

public class SimpleInstance<L> implements INumericLabeledAttributeArrayInstance<L> {
	/**
	 *
	 */
	private static final long serialVersionUID = -6945848041078727475L;

	private final OneHotEncodingTransformer oneHotEncoder = new OneHotEncodingTransformer();

	private final List<IAttributeValue<?>> attributeValues;
	private final L targetValue;

	public SimpleInstance(final List<IAttributeValue<?>> attributeValues, final L targetValue) {
		this.attributeValues = attributeValues;
		this.targetValue = targetValue;
	}

	@Override
	public <T> IAttributeValue<T> getAttributeValueAtPosition(final int position, final Class<T> type) {
		return (IAttributeValue<T>) this.attributeValues.get(position);
	}

	@Override
	public L getTargetValue() {
		return this.targetValue;
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
		sb.append(this.targetValue);
		return sb.toString();
	}

	@Override
	public IAttributeValue<Double> getAttributeValue(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAttributeValue<?>[] getAllAttributeValues() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNumberOfAttributes() {
		// TODO Auto-generated method stub
		return 0;
	}
}
