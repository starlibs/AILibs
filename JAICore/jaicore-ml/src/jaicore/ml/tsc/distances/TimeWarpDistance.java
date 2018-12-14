package jaicore.ml.tsc.distances;

import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeValue;
import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
import jaicore.ml.tsc.util.TimeSeriesUtil;

/**
 * TimeWarpDistance
 */
public class TimeWarpDistance implements IDistance {

    @Override
    public double distance(TimeSeriesAttributeValue timeSeries1, TimeSeriesAttributeValue timeSeries2)
            throws TimeSeriesLengthException {
        TimeSeriesUtil.sameLengthOrException(timeSeries1, timeSeries2);

        int n = (int) timeSeries1.getValue().length();

        double[][] matrix = new double[n + 1][n + 1];

        // Initialization.
        for (int i = 1; i <= n; i++)
            matrix[i][0] = Double.MAX_VALUE;
        for (int i = 1; i <= n; i++)
            matrix[0][i] = Double.MAX_VALUE;
        matrix[0][0] = 0d;

        for (int i = 0; i <= n; i++) {
            for (int j = 0; i <= n; i++) {
                double cost = Math.abs(timeSeries1.getValue().getDouble(i) - timeSeries2.getValue().getDouble(j));
                double mini = Math.min(matrix[i - 1][j], Math.min(matrix[i][j - 1], matrix[i - 1][j - 1]));
                matrix[i][j] = cost + mini;
            }
        }

        return matrix[n][n];
    }

}