package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;

/**
 * A {@link MultidimensionalAttribute} that holds Threedimensional DoubleArrays
 *
 * @author Lukas
 *
 */
public class ThreeDimensionalAttribute extends MultidimensionalAttribute<double[][][]> {

	private static final long serialVersionUID = 6878673196924994437L;
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
	 * {@inheritDoc} takes object type double[][][] or {@link ThreeDimensionalAttributeValue} and returns a {@link ThreeDimensionalAttributeValue} that holds the same values
	 */
	@Override
	public IAttributeValue getAsAttributeValue(final Object object) {
		if (object instanceof double[][][]) {
			return new ThreeDimensionalAttributeValue(this, (double[][][]) object);
		} else if (object instanceof TwoDimensionalAttributeValue) {
			return new ThreeDimensionalAttributeValue(this, ((ThreeDimensionalAttributeValue) object).getValue());
		}

		throw new IllegalArgumentException("No valid value for this attribute");

	}

	@Override
	public boolean isValidValue(final Object value) {
		return (value instanceof ThreeDimensionalAttributeValue || value instanceof double[][][]);
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[3d] " + this.getName(); // TODO check with Tanja if i can just choose String descriptors like this
	}

	@Override
	public double[][][] deserializeAttributeValue(final String string) {
		String formatstring = string.replaceAll(this.OPEN_OR_CLOSED_BRACES_REGEX, this.EMPTY_STRING);
		String[] stringvalues = formatstring.split(this.SINGLE_SPACE);
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
