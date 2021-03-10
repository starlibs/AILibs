package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;

public class MultidimensionalAttribute3d extends MultidimensionalAttribute<double[][][]> {

	/**
	 *
	 */
	private static final long serialVersionUID = 6878673196924994437L; // TODO what is that
	private int xsize;// TODO check with Tanja if xsize and ysize should be in MzltidimensionalAttribute
	private int ysize;
	private int zsize;

	public MultidimensionalAttribute3d(final String name, final int xsize, final int ysize, final int zsize) {
		super(name);
		this.xsize = xsize;
		this.ysize = ysize;
		this.zsize = zsize;
	}

	/**
	 * {@inheritDoc} takes object type double[][][] or MultidimensionalAttributeValue3d - parsed to MultidimensionalAttributeValue3d
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
		return "[MDA3] " + this.getName(); // TODO check with Tanja if i can just choose String descriptors like this
	}

	@Override
	public double[][][] deserializeAttributeValue(final String string) {
		String formatstring = string.replaceAll(this.OPEN_OR_CLOSED_BRACES_REGEX, this.EMPTY_STRING);
		String[] stringvalues = formatstring.split(this.SINGLE_SPACE); // TODO check those 2 lines could be in upper class in theory if MultidimensionalAttribute2d got changed accordingly. The lower lines could be
		// done by a new private Method that is definied in the upper class and lower classes have to implement
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
