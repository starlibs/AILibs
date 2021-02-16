package ai.libs.jaicore.ml.scikitwrapper;

import org.api4.java.algorithm.exceptions.AlgorithmException;

public class ScikitLearnWrapperExecutionFailedException extends AlgorithmException {

	private static final long serialVersionUID = -3658570286117660941L;

	/**
	 * Creates a new {@link ScikitLearnWrapperExecutionFailedException} with the given parameters.
	 *
	 * @param message The message of this {@link Exception}.
	 * @param cause The underlying cause of this {@link Exception}.
	 */
	public ScikitLearnWrapperExecutionFailedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new {@link ScikitLearnWrapperExecutionFailedException} with the given parameters.
	 *
	 * @param message The message of this {@link Exception}.
	 */
	public ScikitLearnWrapperExecutionFailedException(final String message) {
		super(message);
	}

}
