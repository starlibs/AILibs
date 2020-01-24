package ai.libs.jaicore.ml.core.dataset;

import java.util.HashMap;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;

import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;

public class MapInstance extends HashMap<IAttribute, Object> implements IClusterableInstance {

	private static final long serialVersionUID = -5548696792257032346L;
	private final transient ILabeledInstanceSchema scheme;
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
		return this.scheme.getAttributeList().stream().map(this::get).collect(Collectors.toList()).toArray();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.labelAttribute == null) ? 0 : this.labelAttribute.hashCode());
		result = prime * result + ((this.scheme == null) ? 0 : this.scheme.hashCode());
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
		MapInstance other = (MapInstance) obj;
		if (this.labelAttribute == null) {
			if (other.labelAttribute != null) {
				return false;
			}
		} else if (!this.labelAttribute.equals(other.labelAttribute)) {
			return false;
		}
		if (this.scheme == null) {
			if (other.scheme != null) {
				return false;
			}
		} else if (!this.scheme.equals(other.scheme)) {
			return false;
		}
		return true;
	}
}
