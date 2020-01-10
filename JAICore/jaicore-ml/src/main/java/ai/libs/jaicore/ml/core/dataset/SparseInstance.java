package ai.libs.jaicore.ml.core.dataset;

import java.util.Map;
import java.util.stream.IntStream;

public class SparseInstance extends AInstance {

	public static final ENullElement DEF_NULL_ELEMENT = ENullElement.ZERO;

	/**
	 * Determines a default interpretation of values not contained in the map of attributes.
	 * An attribute value can - by default - be understood either to be unknown or zero.
	 */
	public enum ENullElement {
		UNKNOWN, ZERO;
	}

	private ENullElement nullElement;

	private Map<Integer, Object> attributeMap;
	private int numAttributes;

	public SparseInstance(final int numAttributes, final Map<Integer, Object> attributes, final Object label) {
		super(label);
		this.numAttributes = numAttributes;
		this.attributeMap = attributes;
	}

	@Override
	public Object getAttributeValue(final int pos) {
		if (this.attributeMap.containsKey(pos)) {
			return this.attributeMap.get(pos);
		} else if (this.nullElement != null) {
			switch (this.nullElement) {
			case UNKNOWN:
				return "?";
			case ZERO:
				return 0;
			default:
				throw new UnsupportedOperationException("The use of the specified null element is not defined.");
			}
		}
		else {
			return null;
		}
	}

	@Override
	public Object[] getAttributes() {
		return IntStream.range(0, this.numAttributes).mapToObj(this::getAttributeValue).toArray();
	}

	@Override
	public double[] getPoint() {
		double[] point = new double[this.numAttributes];
		for (int i = 0; i < this.numAttributes; i++) {
			point[i] = this.getPointValue(i);
		}
		return point;
	}

	@Override
	public double getPointValue(final int pos) {
		return this.attributeMap.containsKey(pos) ? (double)this.attributeMap.get(pos) : 0;
	}

	@Override
	public void setAttributeValue(final int pos, final Object value) {
		if ((this.nullElement == ENullElement.ZERO && value.equals(0)) || (this.nullElement == ENullElement.UNKNOWN && value.equals("?"))) {
			return;
		}
		this.attributeMap.put(pos, value);
	}

	public Map<Integer, Object> getAttributeMap() {
		return this.attributeMap;
	}

	@Override
	public void removeColumn(final int columnPos) {
		this.attributeMap.remove(columnPos);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.attributeMap == null) ? 0 : this.attributeMap.hashCode());
		result = prime * result + ((this.nullElement == null) ? 0 : this.nullElement.hashCode());
		result = prime * result + this.numAttributes;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		SparseInstance other = (SparseInstance) obj;
		if (this.attributeMap == null) {
			if (other.attributeMap != null) {
				return false;
			}
		} else if (!this.attributeMap.equals(other.attributeMap)) {
			return false;
		}
		if (this.nullElement != other.nullElement) {
			return false;
		}
		return (this.numAttributes == other.numAttributes);
	}

	@Override
	public String toString() {
		return "SparseInstance [nullElement=" + this.nullElement + ", attributeMap=" + this.attributeMap + ", numAttributes=" + this.numAttributes + "]";
	}
}
