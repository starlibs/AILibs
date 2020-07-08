package ai.libs.mlplan.exception;

import ai.libs.jaicore.ml.core.exception.UncheckedJaicoreMLException;
import ai.libs.mlplan.core.IProblemType;

/**
 * The {@link UnsupportedProblemTypeException} indicates that a {@link IProblemType} is chosen, that is unsupported for the ML-Plan version in use. Details concerning the error can be inferred from the associated message.
 *
 * @author Tanja Tornede
 *
 */
public class UnsupportedProblemTypeException extends UncheckedJaicoreMLException {

	private static final long serialVersionUID = 1251668494400378438L;

	/**
	 * Creates a new {@link UnsupportedProblemTypeException} with the given parameters.
	 *
	 * @param message The message of this {@link Exception}.
	 * @param cause The underlying cause of this {@link Exception}.
	 */
	public UnsupportedProblemTypeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new {@link UnsupportedProblemTypeException} with the given parameters.
	 *
	 * @param message The message of this {@link Exception}.
	 */
	public UnsupportedProblemTypeException(final String message) {
		super(message);
	}

}