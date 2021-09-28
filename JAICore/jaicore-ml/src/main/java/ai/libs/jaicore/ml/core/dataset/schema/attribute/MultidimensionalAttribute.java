package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Arrays;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IObjectAttribute;

/**
 * This is an {@link IAttribute} class that holds Multidimensional Double Arrays.
 *
 * @author Lukas Fehring
 *
 * @param <O> O is the type of the {@link MultidimensionalAttributeValue} that is parsed using this {@link IAttribute}. This should be a double array of dimension 2 or 3.
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
	 * {@inheritDoc} This method takes and parameter of type {@link MultidimensionalAttributeValue} or O and serializes it. The resulting form should look like [[a b] [c d] ... [e f]].
	 */
	@Override
	public String serializeAttributeValue(final Object value) {
		Object[] castvalue = (Object[]) value;
		String serialisedString = Arrays.deepToString(castvalue);
		return serialisedString.replace(MultidimensionalAttribute.ARRAY_STRING_SPLITTER, MultidimensionalAttribute.SINGLE_SPACE).replaceAll(MultidimensionalAttribute.WHITESPACE_REGEX, MultidimensionalAttribute.SINGLE_SPACE);
	}

	protected abstract O formGenericMultidimensionalArray(String[] values);

	@Override
	public O deserializeAttributeValue(final String string) {
		String formatstring = string.replaceAll(MultidimensionalAttribute.OPEN_OR_CLOSED_BRACES_REGEX, MultidimensionalAttribute.EMPTY_STRING);
		String[] stringvalues = formatstring.split(MultidimensionalAttribute.SINGLE_SPACE);

		return this.formGenericMultidimensionalArray(stringvalues);

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
