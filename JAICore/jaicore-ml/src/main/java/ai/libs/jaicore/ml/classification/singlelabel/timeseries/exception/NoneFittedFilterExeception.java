package ai.libs.jaicore.ml.classification.singlelabel.timeseries.exception;

import ai.libs.jaicore.ml.core.exception.UncheckedJaicoreMLException;

public class NoneFittedFilterExeception extends UncheckedJaicoreMLException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public NoneFittedFilterExeception(final String message, final Throwable cause) {
		super(message, cause);
	}

	public NoneFittedFilterExeception(final String message) {
		super(message);
	}
}
