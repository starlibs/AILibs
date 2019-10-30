package ai.libs.jaicore.ml.core.dataset;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class DenseInstance extends AInstance {

	private List<Object> attributes;

	public DenseInstance(final Object[] attributes, final Object label) {
		this(Arrays.asList(attributes), label);
	}

	public DenseInstance(final List<Object> attributes, final Object label) {
		super(label);
		this.attributes = attributes;
	}

	@Override
	public Object getAttributeValue(final int pos) {
		return this.attributes.get(pos);
	}

	@Override
	public void setAttributeValue(final int pos, final Object value) {
		this.attributes.remove(pos);
		this.attributes.add(pos, value);
	}

	@Override
	public Object[] getAttributes() {
		return this.attributes.toArray();
	}

	@Override
	public double[] getPoint() {
		return IntStream.range(0, this.attributes.size())
				.mapToDouble(x -> (this.attributes.get(x) instanceof String) ? 0 : (this.attributes.get(x) instanceof Double) ? (Double) this.attributes.get(x) : (double) ((Integer) this.attributes.get(x))).toArray();
	}

	@Override
	public double getPointValue(final int pos) {
		return this.getPoint()[pos];
	}

	@Override
	public void removeColumn(final int columnPos) {
		this.attributes.remove(columnPos);
	}

}
