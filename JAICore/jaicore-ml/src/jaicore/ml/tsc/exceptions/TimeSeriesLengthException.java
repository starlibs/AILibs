package jaicore.ml.tsc.exceptions;

import jaicore.ml.core.exception.CheckedJaicoreMLException;

/**
 * Exception class encapsultaing faulty behaviour with lenght of time series.
 */
public class TimeSeriesLengthException extends CheckedJaicoreMLException {

    private static final long serialVersionUID = 1L;

    public TimeSeriesLengthException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimeSeriesLengthException(String message) {
        super(message);
    }

}