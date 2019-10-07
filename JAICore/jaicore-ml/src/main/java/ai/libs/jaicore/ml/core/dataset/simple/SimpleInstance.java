package ai.libs.jaicore.ml.core.dataset.simple;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;

public class SimpleInstance implements ILabeledInstance, IClusterableInstance {

	private ILabeledInstanceSchema schema;
	private List<Object> attributeValues;
	private Object label;

	public SimpleInstance(final ILabeledInstanceSchema schema, final List<Object> attributeValues, final Object label) {
		this.schema = schema;
		this.attributeValues = attributeValues;
		this.label = label;
	}

	@Override
	public Object getAttributeValue(final int pos) {
		return this.attributeValues.get(pos);
	}

	@Override
	public double[] getPoint() {
		double[] point = new double[this.attributeValues.size()];
		for (int i = 0; i < this.attributeValues.size(); i++) {
			point[i] = this.schema.getAttribute(i).toDouble(this.attributeValues.get(i));
		}
		return point;
	}

	@Override
	public double getPointValue(final int pos) {
		return this.schema.getAttribute(pos).toDouble(this.attributeValues.get(pos));
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		return this.schema;
	}

	@Override
	public Object getLabel() {
		return this.label;
	}

	@Override
	public Object[] getAttributes() {
		return this.attributeValues.toArray();
	}

}
