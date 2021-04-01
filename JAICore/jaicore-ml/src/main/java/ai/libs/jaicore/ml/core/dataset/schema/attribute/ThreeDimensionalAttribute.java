package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;

/**
 * A {@link MultidimensionalAttribute} that holds Threedimensional DoubleArrays
 *
 * @author Lukas
 *
 */
public class ThreeDimensionalAttribute extends MultidimensionalAttribute<double[][][]> {

	private static final long serialVersionUID = 3932496459363695399L;
	private int xsize;
	private int ysize;
	private int zsize;

	public ThreeDimensionalAttribute(final String name, final int xsize, final int ysize, final int zsize) {
		super(name);
		this.xsize = xsize;
		this.ysize = ysize;
		this.zsize = zsize;
	}

	/**
	 * {@inheritDoc} takes object type double[][][] or {@link MultidimensionalAttributeValue} and returns a {@link MultidimensionalAttributeValue} that holds the same values
	 */
	@Override
	public IAttributeValue getAsAttributeValue(final Object object) {
		if (object instanceof double[][][]) {
			return new MultidimensionalAttributeValue<double[][][]>(this, (double[][][]) object);
		} else if (object instanceof MultidimensionalAttributeValue) {
			return new MultidimensionalAttributeValue<double[][][]>(this, (double[][][]) ((MultidimensionalAttributeValue) object).getValue());

		}

		throw new IllegalArgumentException("No valid value for this attribute");
	}

	@Override
	public boolean isValidValue(final Object value) {
		return (value instanceof MultidimensionalAttributeValue || value instanceof double[][][]);
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[3d] " + this.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + this.xsize;
		result = prime * result + this.ysize;
		result = prime * result + this.zsize;
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
		ThreeDimensionalAttribute other = (ThreeDimensionalAttribute) obj;
		if (this.xsize != other.xsize) {
			return false;
		}
		if (this.ysize != other.ysize) {
			return false;
		}
		if (this.zsize != other.zsize) {
			return false;
		}
		return true;
	}

	@Override
	public double[][][] formGenereicMultidimensionalArray(final String[] stringvalues) {
		double[][][] doublevalues = new double[this.xsize][this.ysize][this.zsize];

		int position = 0;
		for (int x = 0; x < this.xsize; x++) {
			double[][] nextdouble = new double[this.ysize][this.zsize];
			for (int y = 0; y < this.ysize; y++) {
				double[] nextsingle = new double[this.zsize];
				for (int z = 0; z < this.zsize; z++) {
					nextsingle[z] = Double.parseDouble(stringvalues[position]);
					position++;
				}
				nextdouble[y] = nextsingle;
			}
			doublevalues[x] = nextdouble;
		}

		return doublevalues;

	}

}
