package jaicore.ml.tsc.exceptions;

/**
 * Exception class encapsultes faulty behaviour with length of time series.
 * 
 * @author fischor
 */
public class TimeSeriesLengthException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public TimeSeriesLengthException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimeSeriesLengthException(String message) {
        super(message);
    }

}