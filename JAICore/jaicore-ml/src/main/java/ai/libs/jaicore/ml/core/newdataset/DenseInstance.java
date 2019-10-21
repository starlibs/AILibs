package ai.libs.jaicore.ml.core.newdataset;

public class DenseInstance extends AInstance {

	private Object[] attributes;

	public DenseInstance(final Object[] attributes, final Object label) {
		super(label);
		this.attributes = attributes;
	}

	@Override
	public Object getAttributeValue(final int pos) {
		return this.attributes[pos];
	}

	@Override
	public void setAttributeValue(final int pos, final Object value) {
		this.attributes[pos] = value;
	}

	@Override
	public Object[] getAttributes() {
		return this.attributes;
	}

	@Override
	public double[] getPoint() {
		throw new UnsupportedOperationException("Not yet implemented in DenseInstance.");
	}

	@Override
	public double getPointValue(final int pos) {
		throw new UnsupportedOperationException("Not yet implemented in DenseInstance.");
	}

}
