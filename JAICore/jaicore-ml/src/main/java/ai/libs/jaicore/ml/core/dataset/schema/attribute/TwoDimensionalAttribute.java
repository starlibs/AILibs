package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;

/**
 * A {@link MultidimensionalAttribute} that holds Twodimensional DoubleArrays
 *
 * @author Lukas Fehring
 *
 */
public class TwoDimensionalAttribute extends MultidimensionalAttribute<double[][]> {

	private static final long serialVersionUID = -3300254871190010390L;
	private int xsize;
	private int ysize;

	public int getXsize() {
		return this.xsize;
	}

	public int getYsize() {
		return this.ysize;
	}

	public TwoDimensionalAttribute(final String name, final int xsize, final int ysize) {
		super(name);
		this.xsize = xsize;
		this.ysize = ysize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + this.xsize;
		result = prime * result + this.ysize;
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
		TwoDimensionalAttribute other = (TwoDimensionalAttribute) obj;
		if (this.xsize != other.xsize) {
			return false;
		}
		if (this.ysize != other.ysize) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc} This method takes a parameter of type double[][] or {@link TwoDimensionalAttributeValue} and returns a {@link TwoDimensionalAttributeValue} that holds the same values
	 */
	@Override
	public IAttributeValue getAsAttributeValue(final Object object) {
		if (object instanceof double[][]) {
			return new TwoDimensionalAttributeValue(this, (double[][]) object);
		} else if (object instanceof TwoDimensionalAttributeValue) {
			return new TwoDimensionalAttributeValue(this, ((TwoDimensionalAttributeValue) object).getValue());
		}

		throw new IllegalArgumentException("No valid value for this attribute");

	}

	@Override
	public boolean isValidValue(final Object value) {
		return (value instanceof TwoDimensionalAttributeValue || value instanceof double[][]);
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[2D] " + this.getName();
	}

	/**
	 * {@inheritDoc} parses String string to MutidimensionalAttributeValue
	 */
	@Override
	public double[][] deserializeAttributeValue(final String string) {
		String arraystring = string.replaceAll(this.SINGLE_SPACE, this.ARRAY_STRING_SPLITTER); // TODO Refactor
		String[] test = arraystring.split(this.INNTER_ARRAY_SPLITTER);
		test[0] = test[0].substring(2);
		test[test.length - 1] = test[test.length - 1].substring(0, test[test.length - 1].length() - 2);
		double[][] values = new double[this.xsize][this.ysize];
		for (int i = 0; i < test.length; i++) {
			String[] tmp = test[i].split(this.ARRAY_STRING_SPLITTER);
			for (int j = 0; j < tmp.length; j++) {
				values[i][j] = Double.parseDouble(tmp[j]);
			}
		}
		return values;
	}

}
