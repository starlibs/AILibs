package ai.libs.jaicore.ml.core.dataset.weka;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.api4.java.ai.ml.core.dataset.ContainsNonNumericAttributesException;
import org.api4.java.ai.ml.core.dataset.attribute.IAttributeType;
import org.api4.java.ai.ml.core.dataset.attribute.IAttributeValue;
import org.api4.java.ai.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import org.api4.java.ai.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import org.api4.java.ai.ml.core.dataset.attribute.primitive.BooleanAttributeType;
import org.api4.java.ai.ml.core.dataset.attribute.primitive.BooleanAttributeValue;
import org.api4.java.ai.ml.core.dataset.attribute.primitive.NumericAttributeType;
import org.api4.java.ai.ml.core.dataset.attribute.primitive.NumericAttributeValue;

import ai.libs.jaicore.basic.sets.ElementDecorator;
import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.ml.core.dataset.InstanceSchema;
import ai.libs.jaicore.ml.core.dataset.sampling.IClusterableInstances;
import weka.core.Attribute;
import weka.core.Instance;

public class WekaInstance<L> extends ElementDecorator<Instance> implements IClusterableInstances<L> {

	public WekaInstance(final Instance instance) {
		super(instance);
	}

	@Override
	public <T> IAttributeValue<T> getAttributeValueAtPosition(final int position, final Class<T> type) {
		return this.getAttributeValue(this.getElement().attribute(position), type);
	}

	private <T> IAttributeValue<T> getAttributeValue(final Attribute a, final Class<T> type) {
		IAttributeType<?> t = WekaInstancesUtil.transformWEKAAttributeToAttributeType(a);
		if (t instanceof BooleanAttributeType) {
			return (IAttributeValue<T>) new BooleanAttributeValue((BooleanAttributeType) t, this.getElement().value(a) == 1.0);
		}
		if (t instanceof CategoricalAttributeType) {
			return (IAttributeValue<T>) new CategoricalAttributeValue((CategoricalAttributeType) t, this.getElement().stringValue(a));
		}
		if (t instanceof NumericAttributeType) {
			return (IAttributeValue<T>) new NumericAttributeValue((NumericAttributeType) t, this.getElement().value(a));
		}
		throw new IllegalArgumentException("Type " + type + " is not a valid type!");

	}

	@Override
	public L getTargetValue() {
		IAttributeType<L> t = (IAttributeType<L>)WekaInstancesUtil.transformWEKAAttributeToAttributeType(this.getElement().classAttribute());
		double classValueAsDouble = this.getElement().classValue();
		return t.buildAttributeValue((t instanceof CategoricalAttributeType) ? WekaUtil.getClassName(this.getElement()) : classValueAsDouble).getValue();
	}

	@Override
	public double[] getAsDoubleVector() throws ContainsNonNumericAttributesException {
		return this.getElement().toDoubleArray();
	}

	@Override
	public IAttributeValue<Double> getAttributeValue(final int position) {
		throw new UnsupportedOperationException();
	}

	public InstanceSchema<L> getSchema() {
		List<IAttributeType<?>> attributeTypeList = new LinkedList<>();
		for (int i = 0; i < this.getElement().numAttributes(); i++) {
			if (i != this.getElement().classIndex()) {
				attributeTypeList.add(WekaInstancesUtil.transformWEKAAttributeToAttributeType(this.getElement().attribute(i)));
			}
		}
		IAttributeType<L> targetType = (IAttributeType<L>) WekaInstancesUtil.transformWEKAAttributeToAttributeType(this.getElement().classAttribute());
		return new InstanceSchema<>(attributeTypeList, targetType);
	}

	@Override
	public IAttributeValue<?>[] getAllAttributeValues() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNumberOfAttributes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.getElement().toDoubleArray()).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof WekaInstance)) {
			return false;
		}
		return WekaUtil.areInstancesEqual(this.getElement(), ((WekaInstance<?>)obj).getElement());
	}
}
