package ai.libs.jaicore.ml.core.newdataset;

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
		} else {
			switch (this.nullElement) {
			case UNKNOWN:
				return "?";
			case ZERO:
				return 0;
			default:
				throw new UnsupportedOperationException("The use of the specified null element is not defined.");
			}
		}
	}

	@Override
	public Object[] getAttributes() {
		return IntStream.range(0, this.numAttributes).mapToObj(x -> this.getAttributeValue(x)).toArray();
	}

	@Override
	public double[] getPoint() {
		throw new UnsupportedOperationException("Not yet implemented in SparseInstance.");// TODO
	}

	@Override
	public double getPointValue(final int pos) {
		throw new UnsupportedOperationException("Not yet implemented in SparseInstance");// TODO
	}

	@Override
	public void setAttributeValue(final int pos, final Object value) {
		if (this.nullElement == ENullElement.ZERO && value.equals(0)) {
			return;
		} else if (this.nullElement == ENullElement.UNKNOWN && value.equals("?")) {
			return;
		}
		this.attributeMap.put(pos, value);
	}

	@Override
	public void removeColumn(final int columnPos) {
		this.attributeMap.remove(columnPos);
	}

}
