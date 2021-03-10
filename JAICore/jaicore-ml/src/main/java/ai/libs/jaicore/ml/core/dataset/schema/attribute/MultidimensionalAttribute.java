package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Arrays;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IObjectAttribute;

public abstract class MultidimensionalAttribute<O> extends AGenericObjectAttribute<O> implements IObjectAttribute<NumericAttribute> {

	private static final long serialVersionUID = -1416829149499898655L; // TODO What is an UID used for in this context - The same as in distributed Algorithms? -> is this done in a distributed Fashion

	// TODO should constants have a comment ? In some guidelines they do in some they don't
	protected final String SINGLE_SPACE = " ";
	protected final String ARRAY_STRING_SPLITTER = ",";
	protected final String EMPTY_STRING = "";
	protected final String OPEN_OR_CLOSED_BRACES_REGEX = "\\[|\\]";
	protected final String INNTER_ARRAY_SPLITTER = "\\],\\[";
	protected final String DOUBLE_SPACE = "  ";

	protected MultidimensionalAttribute(final String name) {
		super(name);
	}

	@Override
	public double toDouble(final Object object) {
		throw new UnsupportedOperationException("Not yet implemented in MultidimensionalAttribute"); // TODO check what is with those methods now.
	}

	/**
	 * {@inheritDoc} takes object of type MultidimensionalAttributeValue - parses it to [[a b] [c d] [e f]]
	 */
	@Override
	public String serializeAttributeValue(final Object value) {
		Object[] castvalue = (Object[]) value;
		String serialisedString = Arrays.deepToString(castvalue);
		return serialisedString.replace(this.ARRAY_STRING_SPLITTER, this.SINGLE_SPACE).replace(this.DOUBLE_SPACE, this.SINGLE_SPACE);
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
