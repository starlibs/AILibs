package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;

public class MultidimensionalAttribute3d extends MultidimensionalAttribute<double[][][]> {

	private int xsize;
	private int ysize;
	private int zsize;

	private static final long serialVersionUID = 1L;

	public MultidimensionalAttribute3d(final String name, final int i, final int j, final int k) {
		super(name);
		this.xsize = i;
		this.ysize = j;
		this.zsize = k;
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
	public double[][][] deserializeAttributeValue(final String string) {// TODO TESTCASE if it works maybe chagne 2d version
		String formatstring = string.replaceAll(this.OPEN_OR_CLOSED_BRACES_REGEX, this.EMPTY_STRING);
		String[] stringvalues = formatstring.split(this.INPUTSTRING_INNER_SPLITTER);
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
