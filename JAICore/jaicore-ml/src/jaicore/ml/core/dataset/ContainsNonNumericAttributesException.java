package jaicore.ml.core.dataset;

import jaicore.ml.core.exception.CheckedJaicoreMLException;

public class ContainsNonNumericAttributesException extends CheckedJaicoreMLException {
	/**
	 * Automatically generated version UID for serialization.
	 */
	private static final long serialVersionUID = 3998118925791147399L;

	public ContainsNonNumericAttributesException(final String message) {
		super(message);
	}

	public ContainsNonNumericAttributesException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
