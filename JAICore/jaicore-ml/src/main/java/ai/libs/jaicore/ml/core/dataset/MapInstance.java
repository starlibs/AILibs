package ai.libs.jaicore.ml.core.dataset;

import java.util.HashMap;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;

import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;

public class MapInstance extends HashMap<IAttribute, Object> implements IClusterableInstance {

	private final ILabeledInstanceSchema scheme;
	private final IAttribute labelAttribute;

	public MapInstance(final ILabeledInstanceSchema scheme, final IAttribute labelAttribute) {
		super();
		this.scheme = scheme;
		this.labelAttribute = labelAttribute;
	}

	@Override
	public void setAttributeValue(final int pos, final Object value) {
		this.put(this.scheme.getAttribute(pos), value);
	}

	@Override
	public Object[] getAttributes() {
		return this.scheme.getAttributeList().stream().map(a -> this.get(a)).collect(Collectors.toList()).toArray();
	}

	@Override
	public double[] getPoint() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeColumn(final int columnPos) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getLabel() {
		return this.get(this.labelAttribute);
	}

	@Override
	public void setLabel(final Object obj) {
		this.put(this.labelAttribute, obj);
	}
}
