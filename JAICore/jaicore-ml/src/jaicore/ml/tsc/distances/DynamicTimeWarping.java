package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
import jaicore.ml.tsc.util.TimeSeriesUtil;

/**
 * Class for the Time Warp Distance Calculation.
 * 
 * <p>
 * Let <code>A = {1, 1, 2, 2, 3, 5}</code> and
 * <code>B = {1, 1, 2, 2, 3, 5}</code> be two time series. Let <code>n=6</code>
 * and <code>m=7</code> be the length of <code>A</code> and <code>B</code>
 * respec. Define the distance between to points <code>x</code> and
 * <code>y</code> as <code>d(x, y) = |x - y|</code> as their absolute
 * difference.
 * </p>
 * 
 * <p>
 * The distance matrix <code>M</code> will look like this:
 * </p>
 * <p>
 * <code>
 * +------+------+------+------+------+------+------+------+
 * |      |   0  |   1  |   1  |   2  |   2  |   3  |   5  |
 * +------+------+------+------+------+------+------+------+
 * |   0  |   0  |  inf |  inf |  inf |  inf |  inf |  inf |
 * +------+------+------+------+------+------+------+------+
 * |   1  |  inf |   0  |   0  |   1  |   2  |   4  |   8  |
 * +------+------+------+------+------+------+------+------+
 * |   2  |  inf |   1  |   1  |   0  |   0  |   1  |   4  |
 * +------+------+------+------+------+------+------+------+
 * |   3  |  inf |   3  |   3  |   1  |   1  |   0  |   2  |
 * +------+------+------+------+------+------+------+------+
 * |   5  |  inf |   7  |   7  |   4  |   4  |   2  |   0  |
 * +------+------+------+------+------+------+------+------+        
 * |   5  |  inf |  11  |  11  |   7  |   7  |   4  |   0  |        
 * +------+------+------+------+------+------+------+------+            
 * |   5  |  inf |  15  |  15  |  10  |  10  |   6  |   0  |            
 * +------+------+------+------+------+------+------+------+            
 * |   6  |  inf |  20  |  20  |  14  |  14  |   9  |   1  |            
 * +------+------+------+------+------+------+------+------+     
 * </code>
 * </p>
 * 
 * References:
 * <ul>
 * <li>https://riptutorial.com/algorithm/example/24981/introduction-to-dynamic-time-warping</li>
 * </ul>
 */
public class DynamicTimeWarping implements ITimeSeriesDistance {

    /** Distance measure for scalar points. */
    IScalarDistance d;

    /**
     * Creates an instance with absolute distance as point distance.
     */
    public DynamicTimeWarping() {
        this((x, y) -> Math.abs(x - y));
    }

    /**
     * Creates an instance with a given scalar distance measure.
     * 
     * @param d Scalar distance measure.
     */
    public DynamicTimeWarping(IScalarDistance d) {
        this.d = d;
    }

    @Override
    public double distance(double[] A, double[] B) throws TimeSeriesLengthException {
        // Care in the most algorithm descriptions, the time series are 1-indexed.

        int n = A.length;
        int m = B.length;
        double[][] M = new double[n + 1][m + 1]; // from 0 to n+1 incl. and 0 to m+1 incl.

        // Initialize first row and column to infinity (except [0][0]).
        for (int i = 1; i <= n; i++)
            M[i][0] = Double.MAX_VALUE;
        for (int j = 1; j <= m; j++)
            M[0][j] = Double.MAX_VALUE;
        // Initialize [0][0] with 0.
        M[0][0] = 0d;

        // Dynamic programming.
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = d.distance(A[i - 1], B[j - 1]); // 1 indexed in algo.
                double mini = Math.min(M[i - 1][j], Math.min(M[i][j - 1], M[i - 1][j - 1]));
                M[i][j] = cost + mini;
            }
        }

        return M[n][m];
    }

    public double distanceWithWindow(double[] A, double[] B, int w) {
        int n = A.length;
        int m = B.length;
        double[][] M = new double[n + 1][m + 1];

        w = Math.max(w, Math.abs(n - m));

        // Initialize first row and column to infinity (except [0][0]).
        for (int i = 1; i <= n; i++)
            M[i][0] = Double.MAX_VALUE;
        for (int j = 1; j <= m; j++)
            M[0][j] = Double.MAX_VALUE;
        // Initialize [0][0] with 0.
        M[0][0] = 0d;

        for (int i = 1; i <= n; i++) {
            for (int j = Math.max(1, i - w); j <= Math.min(m, i + w); j++) {
                double cost = d.distance(A[i - 1], B[j - 1]);
                double mini = Math.min(M[i - 1][j], Math.min(M[i][j - 1], M[i - 1][j - 1]));
                M[i][j] = cost + mini;
            }
        }

        return M[n][m];
    }

}