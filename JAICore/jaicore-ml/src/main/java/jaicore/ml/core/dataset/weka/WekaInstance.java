package jaicore.ml.core.dataset.weka;

import jaicore.basic.sets.ElementDecorator;
import jaicore.ml.core.dataset.ContainsNonNumericAttributesException;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.core.dataset.attribute.primitive.BooleanAttributeType;
import jaicore.ml.core.dataset.attribute.primitive.BooleanAttributeValue;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeType;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeValue;
import weka.core.Attribute;
import weka.core.Instance;

public class WekaInstance extends ElementDecorator<Instance> implements IInstance {

	public WekaInstance(final Instance instance) {
		super(instance);
	}

	@Override
	public <T> IAttributeValue<T> getAttributeValue(final int position, final Class<T> type) {
		return this.getAttributeValue(this.getElement().attribute(position), type);
	}

	private <T> IAttributeValue<T> getAttributeValue(final Attribute a, final Class<T> type) {
		IAttributeType<?> t = WekaInstancesUtil.transformWEKAAttributeToAttributeType(a);
		if (t instanceof BooleanAttributeType) {
			return (IAttributeValue<T>)new BooleanAttributeValue((BooleanAttributeType)t, this.getElement().value(a) == 1.0);
		}
		if (t instanceof CategoricalAttributeType) {
			return (IAttributeValue<T>)new CategoricalAttributeValue((CategoricalAttributeType)t, this.getElement().stringValue(a));
		}
		if (t instanceof NumericAttributeType) {
			return (IAttributeValue<T>)new NumericAttributeValue((NumericAttributeType)t, this.getElement().value(a));
		}
		throw new IllegalArgumentException("Type " + type + " is not a valid type!");

	}

	@Override
	public <T> IAttributeValue<T> getTargetValue(final Class<T> type) {
		return this.getAttributeValue(this.getElement().classAttribute(), type);
	}

	@Override
	public double[] getAsDoubleVector() throws ContainsNonNumericAttributesException {
		return this.getElement().toDoubleArray();
	}
}
