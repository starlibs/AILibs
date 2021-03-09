package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;

public class MultidimensionalAttribute3d extends MultidimensionalAttribute<double[][][]> {

	private int xsize;
	private int ysize;
	private int zsize;

	private static final long serialVersionUID = 1L;

	protected MultidimensionalAttribute3d(final String name, final int xsize, final int ysize, final int zsize) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	/**
	 * {@inheritDoc} takes object type double[][] or MultidimensionalAttributeValue - parsed to MultidimensionalAttributeValue
	 */
	@Override
	public IAttributeValue getAsAttributeValue(final Object object) {
		if (object instanceof double[][][]) {
			return new MultidimensionalAttributeValue3d(this, (double[][][]) object);
		} else if (object instanceof MultidimensionalAttributeValue2d) {
			return new MultidimensionalAttributeValue3d(this, ((MultidimensionalAttributeValue3d) object).getValue());
		}

		throw new IllegalArgumentException("No valid value for this attribute");

	}

	@Override
	public boolean isValidValue(final Object value) {
		return (value instanceof MultidimensionalAttributeValue3d || value instanceof double[][][]);
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[MDA3] " + this.getName();
	}

	@Override
	public Object deserializeAttributeValue(final String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
