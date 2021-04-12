package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Arrays;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IObjectAttribute;

/**
 * An Abstract {@link IAttribute} class that holds Multidimensional Double Arrays.
 *
 * @author Lukas Fehring
 *
 * @param <O>
 */
public abstract class MultidimensionalAttribute<O> extends AGenericObjectAttribute<O> implements IObjectAttribute<NumericAttribute> {

	private static final long serialVersionUID = -1416829149499898655L;

	protected static final String SINGLE_SPACE = " ";
	protected static final String ARRAY_STRING_SPLITTER = ",";
	protected static final String EMPTY_STRING = "";
	protected static final String OPEN_OR_CLOSED_BRACES_REGEX = "\\[|\\]";
	protected static final String INNTER_ARRAY_SPLITTER = "\\],\\[";
	protected static final String WHITESPACE_REGEX = "\\s+";

	protected MultidimensionalAttribute(final String name) {
		super(name);
	}

	@Override
	public double toDouble(final Object object) {
		throw new UnsupportedOperationException("Not yet implemented in MultidimensionalAttribute");
	}

	/**
	 * {@inheritDoc} This method takes and parameter of type {@link ThreeDimensionalAttributeValue} or doulbe[][][] and parses it to a serilised value of form[[a b] [c d] [e f]]
	 */
	@Override
	public String serializeAttributeValue(final Object value) {
		Object[] castvalue = (Object[]) value;
		String serialisedString = Arrays.deepToString(castvalue);
		return serialisedString.replace(this.ARRAY_STRING_SPLITTER, this.SINGLE_SPACE).replaceAll(this.WHITESPACE_REGEX, this.SINGLE_SPACE);
	}

	protected abstract O formGenereicMultidimensionalArray(String[] values);

	@Override
	public O deserializeAttributeValue(final String string) {
		String formatstring = string.replaceAll(this.OPEN_OR_CLOSED_BRACES_REGEX, this.EMPTY_STRING);
		String[] stringvalues = formatstring.split(this.SINGLE_SPACE);

		return this.formGenereicMultidimensionalArray(stringvalues);

	}

	@SuppressWarnings("unchecked")
	@Override
	protected O getValueAsTypeInstance(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof MultidimensionalAttributeValue<?>) {
				return (O) ((MultidimensionalAttributeValue<?>) object).getValue();
			} else {
				return (O) object;
			}
		}
		throw new IllegalArgumentException("No valid value for this attribute");

	}

}
