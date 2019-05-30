package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.util.ScalarDistanceUtil;

/**
 * Time Warp Edit Distance as published in "Time Warp Edit Distance with
 * Stiffness Adjustment for Time Series Matching" by Pierre-Francois Marteau
 * (2009).
 *
 * The similarity between two time series is measured as the minimum cost
 * sequence of edit operations needed to transform one time series into another.
 *
 * @author fischor
 */
public class TimeWarpEditDistance implements ITimeSeriesDistanceWithTimestamps {

	/**
	 * Stiffness parameter. Used to parametrize the influence of the time stamp
	 * distance. Must be positive.
	 */
	private double nu;

	/** Additional cost parameter for deletion. Must be positive. */
	private double lambda;

	/**
	 * Distance mesaure used for point distance calculation.
	 */
	private IScalarDistance d;

	/**
	 * Constructor.
	 *
	 * @param lambda Additional cost parameter for deletion.
	 * @param nu     Stiffness parameter. Used to parametrize the influence of the
	 *               time stamp distance.
	 * @param d      Distance mesaure used for point distance calculation.
	 */
	public TimeWarpEditDistance(final double lambda, final double nu, final IScalarDistance d) {
		// Parameter checks.
		if (lambda < 0) {
			throw new IllegalArgumentException("Parameter lambda must be greater or equal to zero.");
		}
		if (nu < 0) {
			throw new IllegalArgumentException("Parameter nu must be greater or equal to zero.");
		}
		if (d == null) {
			throw new IllegalArgumentException("Parameter d must not be null.");
		}

		this.lambda = lambda;
		this.nu = nu;
		this.d = d;
	}

	/**
	 * Constructor. Creates a TimeWarpEditDistance with squared distance as point
	 * distance.
	 *
	 * @param lambda Additional cost parameter for deletion.
	 * @param nu     Stiffness parameter.
	 */
	public TimeWarpEditDistance(final double lambda, final double nu) {
		this(lambda, nu, ScalarDistanceUtil.getSquaredDistance());
	}

	@Override
	public double distance(final double[] a, final double[] tA, final double[] b, final double[] tB) {
		int n = a.length;
		int m = b.length;

		// DP[0..n, 0..m]
		double[][] dp = new double[n + 1][m + 1];

		// declare A[0] := 0, tA[0] := 0
		// declare B[0] := 0, tB[0] := 0
		// Note: Zero pad A and B, i.e. when referencing A[i] use A[i-1], when
		// referencing A[i-1] use A[i-2]

		// Dynamic Programming initialization.
		for (int i = 1; i <= n; i++) {
			dp[i][0] = Double.MAX_VALUE;
		}
		for (int i = 1; i <= m; i++) {
			dp[0][i] = Double.MAX_VALUE;
		}
		dp[0][0] = 0d;

		// Dynamic programming.
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {

				// Cost for Deletion in A.
				double c1;
				// Cost for Deletion in B.
				double c2;
				// Cost for a match.
				double c3;

				if (i == 1 && j == 1) {
					// Substitute A[i-2] with 0 and B[j-2] with 0.
					c1 = dp[i - 1][j] + this.d.distance(0, a[i - 1]) + this.nu * tA[i - 1] + this.lambda;
					c2 = dp[i][j - 1] + this.d.distance(0, b[j - 1]) + this.nu * tB[j - 1] + this.lambda;
					c3 = dp[i - 1][j - 1] + this.d.distance(a[i - 1], b[i - 1]) + this.nu * Math.abs(tA[i - 1] - tB[j - 1]);
				} else if (i == 1) {
					// Substitute A[i-2] with 0.
					c1 = dp[i - 1][j] + this.d.distance(0, a[i - 1]) + this.nu * tA[i - 1] + this.lambda;
					c2 = dp[i][j - 1] + this.d.distance(b[j - 2], b[j - 1]) + this.nu * (tB[j - 1] - tB[j - 2]) + this.lambda;
					c3 = dp[i - 1][j - 1] + this.d.distance(a[i - 1], b[i - 1]) + this.d.distance(0, b[j - 2])
					+ this.nu * (Math.abs(tA[i - 1] - tB[j - 1]) + tB[j - 2]);
				} else if (j == 1) {
					// Substitute B[j-2] with 0.
					c1 = dp[i - 1][j] + this.d.distance(a[i - 2], a[i - 1]) + this.nu * (tA[i - 1] - tA[i - 2]) + this.lambda;
					c2 = dp[i][j - 1] + this.d.distance(0, b[j - 1]) + this.nu * tB[j - 1] + this.lambda;
					c3 = dp[i - 1][j - 1] + this.d.distance(a[i - 1], b[i - 1]) + this.d.distance(a[i - 2], 0)
					+ this.nu * (Math.abs(tA[i - 1] - tB[j - 1]) + tA[i - 2]);
				} else {
					// No substitution.
					c1 = dp[i - 1][j] + this.d.distance(a[i - 2], a[i - 1]) + this.nu * (tA[i - 1] - tA[i - 2]) + this.lambda;
					c2 = dp[i][j - 1] + this.d.distance(b[j - 2], b[j - 1]) + this.nu * (tB[j - 1] - tB[j - 2]) + this.lambda;
					c3 = dp[i - 1][j - 1] + this.d.distance(a[i - 1], b[i - 1]) + this.d.distance(a[i - 2], b[j - 2])
					+ this.nu * (Math.abs(tA[i - 1] - tB[j - 1]) + Math.abs(tA[i - 2] - tB[j - 2]));
				}

				// Minimum cost.
				double minimum = Math.min(c1, Math.min(c2, c3));
				dp[i][j] = minimum;
			}
		}

		return dp[n][m];
	}

}