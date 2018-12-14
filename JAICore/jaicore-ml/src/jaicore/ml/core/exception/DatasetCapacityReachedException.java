package jaicore.ml.core.exception;

import jaicore.ml.core.dataset.TimeSeriesDataset;

/**
 * Exception that indicates that the capacity of a {@link TimeSeriesDataset} is
 * reached. I.e. the maximum nuber of instances is already contained in the
 * dataset.
 */
public class DatasetCapacityReachedException extends CheckedJaicoreMLException {

    private static final long serialVersionUID = 8108652448377411780L;

    /**
     * Creates a new {@link DatasetCapacityReachedException} with the given
     * parameters.
     * 
     * @param message The message of this {@link Exception}.
     * @param cause   The underlying cause of this {@link Exception}.
     */
    public DatasetCapacityReachedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new {@link DatasetCapacityReachedException} with the given
     * parameters.
     * 
     * @param message The message of this {@link Exception}.
     */
    public DatasetCapacityReachedException(String message) {
        super(message);
    }

}