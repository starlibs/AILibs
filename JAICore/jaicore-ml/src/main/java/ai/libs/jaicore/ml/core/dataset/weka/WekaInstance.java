package ai.libs.jaicore.ml.core.dataset.weka;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.api4.java.ai.ml.dataset.attribute.IAttributeType;

import ai.libs.jaicore.basic.sets.ElementDecorator;
import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.ml.core.dataset.InstanceSchema;
import ai.libs.jaicore.ml.core.dataset.sampling.IClusterableInstances;
import weka.core.Attribute;
import weka.core.Instance;

public class WekaInstance extends ElementDecorator<Instance> implements IClusterableInstances<Double> {

	public WekaInstance(final Instance instance) {
		super(instance);
	}

	@Override
	public Double get(final int pos) {
		return this.getElement().value(pos);
	}

	private double getAttributeValue(final Attribute a) {
		return this.getElement().value(a);
	}

	@Override
	public double[] toDoubleVector() {
		return this.getElement().toDoubleArray();
	}

	public InstanceSchema getSchema() {
		List<IAttributeType> attributeTypeList = new LinkedList<>();
		for (int i = 0; i < this.getElement().numAttributes(); i++) {
			if (i != this.getElement().classIndex()) {
				attributeTypeList.add(WekaInstancesUtil.transformWEKAAttributeToAttributeType(this.getElement().attribute(i)));
			}
		}
		IAttributeType targetType = WekaInstancesUtil.transformWEKAAttributeToAttributeType(this.getElement().classAttribute());
		return new InstanceSchema(attributeTypeList, targetType);
	}

	@Override
	public int getNumFeatures() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Double getLabel() {
		return this.getElement().classValue();
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
		return WekaUtil.areInstancesEqual(this.getElement(), ((WekaInstance) obj).getElement());
	}

	@Override
	public Iterator<Double> iterator() {
		return Arrays.stream(this.getElement().toDoubleArray()).iterator();
	}
}
