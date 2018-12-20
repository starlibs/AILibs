package jaicore.ml.tsc.util;

import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeValue;
import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * TimeSeriesUtil
 */
public class TimeSeriesUtil {

    public static void sameLengthOrException(TimeSeriesAttributeValue timeSeries1, TimeSeriesAttributeValue timeSeries2)
            throws TimeSeriesLengthException {
        long length1 = timeSeries1.getValue().length();
        long length2 = timeSeries2.getValue().length();
        if (length1 != length2) {
            String message = String.format(
                    "Length of the given time series are not equal: Length first time series: (%d). Length of seconds time series: (%d)",
                    length1, length2);
            throw new TimeSeriesLengthException(message);
        }
    }
}