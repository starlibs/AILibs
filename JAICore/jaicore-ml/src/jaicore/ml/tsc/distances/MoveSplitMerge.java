package jaicore.ml.tsc.distances;

/**
 * MoveSplitMerge
 */
public class MoveSplitMerge implements ITimeSeriesDistance {

    private double c;

    /**
     * Constructor.
     * 
     * @param c The constant cost for split and merge operations.
     */
    public MoveSplitMerge(double c) {
        this.c = c;
    }

    @Override
    public double distance(double[] A, double[] B) {
        int n = A.length;
        int m = B.length;
        double[][] Cost = new double[n][m];

        // Initialization.
        Cost[0][0] = Math.abs(A[0] - B[0]);
        for (int i = 1; i < n; i++) {
            Cost[i][0] = Cost[i - 1][0] + C(A[i], A[i - 1], B[0]);
        }
        for (int j = 1; j < m; j++) {
            Cost[0][j] = Cost[0][j - 1] + C(B[j], A[0], B[j - 1]);
        }

        // Dynamic programming.
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < m; j++) {
                double costMove = Cost[i - 1][j - 1] + Math.abs(A[i] - B[j]);
                double cost2 = Cost[i - 1][j] + C(A[i], A[i - 1], B[j]);
                double cost3 = Cost[i][j - 1] + C(B[j], A[i], B[j - 1]);
                Cost[i][j] = Math.min(costMove, Math.min(cost2, cost3));
            }
        }

        return Cost[n - 1][m - 1];
    }

    private double C(double x, double xBefore, double y) {
        if ((xBefore <= x && x <= y) || (xBefore >= x && x >= y)) {
            // x_{i-1} <= x_i <= y_j or x_{i-1} >= x_i >= y_j
            return c;
        } else {
            return c + Math.min(Math.abs(x - xBefore), Math.abs(x - y));
        }
    }

}