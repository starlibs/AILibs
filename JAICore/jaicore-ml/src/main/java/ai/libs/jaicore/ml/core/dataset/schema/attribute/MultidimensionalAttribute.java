package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Arrays;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IObjectAttribute;

public abstract class MultidimensionalAttribute<O> extends AGenericObjectAttribute<O> implements IObjectAttribute<NumericAttribute> {

	protected final char INPUTSTRING_INNER_SPLITTER = ' ';
	protected final char ARRAY_STRING_SPLITTER = ',';
	protected final String INNTER_ARRAY_SPLITTER = "\\],\\[";
	protected final String DOUBLE_SPACE = "  ";
	protected final String SPACE = " ";

	protected MultidimensionalAttribute(final String name) {
		super(name);
	}

	@Override
	public double toDouble(final Object object) {
		throw new UnsupportedOperationException("Not yet implemented in MultidimensionalAttribute");
	}

	/**
	 * {@inheritDoc} takes object of type MultidimensionalAttributeValue - parses it to [[a b] [c d] [e f]]
	 */
	@Override
	public String serializeAttributeValue(final Object value) {
		Object[] castvalue = (Object[]) value;
		String serialisedString = Arrays.deepToString(castvalue);
		return serialisedString.replace(this.ARRAY_STRING_SPLITTER, this.INPUTSTRING_INNER_SPLITTER).replace(this.DOUBLE_SPACE, this.SPACE);
	}

	@Override
	protected O getValueAsTypeInstance(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof MultidimensionalAttributeValue<?>) {
				return ((MultidimensionalAttributeValue<O>) object).getValue();
			} else {
				return (O) object;
			}
		}
		throw new IllegalArgumentException("No valid value for this attribute");

	}

}
