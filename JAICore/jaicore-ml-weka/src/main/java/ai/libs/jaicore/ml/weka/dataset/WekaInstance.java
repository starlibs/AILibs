package ai.libs.jaicore.ml.weka.dataset;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.basic.sets.ElementDecorator;
import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
import ai.libs.jaicore.ml.weka.WekaUtil;
import weka.core.Instance;

public class WekaInstance extends ElementDecorator<Instance> implements ILabeledInstance {

	private ILabeledInstanceSchema schema;

	public WekaInstance(final Instance instance) {
		super(instance);
		List<IAttribute> attributeTypeList = new LinkedList<>();
		for (int i = 0; i < this.getElement().numAttributes(); i++) {
			if (i != this.getElement().classIndex()) {
				attributeTypeList.add(WekaInstancesUtil.transformWEKAAttributeToAttributeType(this.getElement().attribute(i)));
			}
		}
		IAttribute targetType = WekaInstancesUtil.transformWEKAAttributeToAttributeType(this.getElement().classAttribute());
		this.schema = new LabeledInstanceSchema(this.getRelationName(), attributeTypeList, targetType);
	}

	public WekaInstance(final ILabeledInstanceSchema schema, final Instance instance) {
		super(instance);
		this.schema = schema;
	}

	@Override
	public int getNumAttributes() {
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
	public Double getAttributeValue(final int pos) {
		return this.getElement().value(pos);
	}

	@Override
	public Object[] getAttributes() {
		return null;
	}

	@Override
	public double[] getPoint() {
		return this.getElement().toDoubleArray();
	}

	@Override
	public double getPointValue(final int pos) {
		return this.getElement().value(pos);
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		return this.schema;
	}
}
