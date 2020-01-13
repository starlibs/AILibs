package ai.libs.jaicore.ml.classification.singlelabel.timeseries.exception;

/**
 * Exception thrown when a time series dataset could not be extracted from an
 * external data source (e. g. a file).
 *
 * @author Julian Lienen
 *
 */
public class TimeSeriesLoadingException extends Exception {

	/**
	 * Default generated serial version UID.
	 */
	private static final long serialVersionUID = -3825008730451093690L;

	/**
	 * Constructor using a nested <code>Throwable</code> exception.
	 *
	 * @param message
	 *            Individual exception message
	 * @param cause
	 *            Nested exception
	 */
	public TimeSeriesLoadingException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Standard constructor.
	 *
	 * @param message
	 *            Individual exception message
	 */
	public TimeSeriesLoadingException(final String message) {
		super(message);
	}
}
