package jaicore.ml.core.optimizing.lbfgs;

import jaicore.ml.core.optimizing.lbfgs.LBFGS.LinesearchAlgorithm;
/**
 * Parameters to control the optimizer. To control convergence/runtime, see:
 * {@link #maxIterations} and {@link #epsilon} To get L1 penalization, see:
 * {@link #orthantwiseC}, {@link #orthantwiseStart}, {@link #orthantwiseEnd}
 */

public class LBFGSParameters {
		/**
		 * The number of corrections to approximate the inverse hessian matrix. The
		 * L-BFGS routine stores the computation results of previous \ref m iterations
		 * to approximate the inverse hessian matrix of the current iteration. This
		 * parameter controls the size of the limited memories (corrections). The
		 * default value is \c 6. Values less than \c 3 are not recommended. Large
		 * values will result in excessive computing time.
		 */
		private int m = 12;

		/**
		 * Epsilon for convergence test. This parameter determines the accuracy with
		 * which the solution is to be found. A minimization terminates when ||g|| <
		 * \ref epsilon * max(1, ||x||), where ||.|| denotes the Euclidean (L2) norm.
		 * The default value is \c 1e-5.
		 */
		private double epsilon = 1e-20;

		/**
		 * Distance for delta-based convergence test. This parameter determines the
		 * distance, in iterations, to compute the rate of decrease of the objective
		 * function. If the value of this parameter is zero, the library does not
		 * perform the delta-based convergence test.
		 */
		private int past = 0; // BTO: changed from '0'

		/**
		 * Delta for convergence test. This parameter determines the minimum rate of
		 * decrease of the objective function. The library stops iterations when the
		 * following condition is met: (f' - f) / f < \ref delta, where f' is the
		 * objective value of \ref past iterations ago, and f is the objective value of
		 * the current iteration. The default value is \c 0.
		 * 
		 * REQUIRES 'past' TO BE SET
		 */
		private double delta = 1e-5;

		/**
		 * The maximum number of iterations. The lbfgs() function terminates an
		 * optimization process with ::LBFGSERR_MAXIMUMITERATION status code when the
		 * iteration count exceedes this parameter. Setting this parameter to zero
		 * continues an optimization process until a convergence or error. The default
		 * value is \c 0.
		 */
		private int maxIterations = 0;

		/**
		 * The line search algorithm. This parameter specifies a line search algorithm
		 * to be used by the L-BFGS routine.
		 */
		// BTO: morethuente was default in LibLBFGS, disabling for now
		LinesearchAlgorithm linesearch = LinesearchAlgorithm.LBFGS_LINESEARCH_BACKTRACKING_WOLFE;

		/**
		 * The maximum number of trials for the line search. This parameter controls the
		 * number of function and gradients evaluations per iteration for the line
		 * search routine. The default value is \c 40.
		 */
		private int maxLinesearch = 40;

		/**
		 * The minimum step of the line search routine. The default value is \c 1e-20.
		 * This value need not be modified unless the exponents are too large for the
		 * machine being used, or unless the problem is extremely badly scaled (in which
		 * case the exponents should be increased).
		 */
		private double minStep = 1e-20;

		/**
		 * The maximum step of the line search. The default value is \c 1e+20. This
		 * value need not be modified unless the exponents are too large for the machine
		 * being used, or unless the problem is extremely badly scaled (in which case
		 * the exponents should be increased).
		 */
		private double maxStep = 1e20;

		/**
		 * A parameter to control the accuracy of the line search routine. The default
		 * value is \c 1e-4. This parameter should be greater than zero and smaller than
		 * \c 0.5.
		 */
		private double ftol = 1e-4;

		/**
		 * A coefficient for the Wolfe condition. This parameter is valid only when the
		 * backtracking line-search algorithm is used with the Wolfe condition,
		 * ::LBFGS_LINESEARCH_BACKTRACKING_STRONG_WOLFE or
		 * ::LBFGS_LINESEARCH_BACKTRACKING_WOLFE . The default value is \c 0.9. This
		 * parameter should be greater the \ref ftol parameter and smaller than \c 1.0.
		 */
		private double wolfe = 0.9;

		/**
		 * A parameter to control the accuracy of the line search routine. The default
		 * value is \c 0.9. If the function and gradient evaluations are inexpensive
		 * with respect to the cost of the iteration (which is sometimes the case when
		 * solving very large problems) it may be advantageous to set this parameter to
		 * a small value. A typical small value is \c 0.1. This parameter shuold be
		 * greater than the \ref ftol parameter (\c 1e-4) and smaller than \c 1.0.
		 */
		private double gtol = 0.9;

		/**
		 * The machine precision for floating-point values. This parameter must be a
		 * positive value set by a client program to estimate the machine precision. The
		 * line search routine will terminate with the status code
		 * (::LBFGSERR_ROUNDING_ERROR) if the relative width of the interval of
		 * uncertainty is less than this parameter.
		 */
		private double xtol = 1e-16;

		/**
		 * Coeefficient for the L1 norm of variables. This parameter should be set to
		 * zero for standard minimization problems. Setting this parameter to a positive
		 * value activates Orthant-Wise Limited-memory Quasi-Newton (OWL-QN) method,
		 * which minimizes the objective function F(x) combined with the L1 norm |x| of
		 * the variables, {F(x) + C |x|}. This parameter is the coeefficient for the
		 * |x|, i.e., C. As the L1 norm |x| is not differentiable at zero, the library
		 * modifies function and gradient evaluations from a client program suitably; a
		 * client program thus have only to return the function value F(x) and gradients
		 * G(x) as usual. The default value is zero.
		 */
		private double orthantwiseC = 0;

		/**
		 * Start index for computing L1 norm of the variables. This parameter is valid
		 * only for OWL-QN method (i.e., \ref orthantwise_c != 0). This parameter b (0
		 * <= b < N) specifies the index number from which the library computes the L1
		 * norm of the variables x, |x| := |x_{b}| + |x_{b+1}| + ... + |x_{N}| . In
		 * other words, variables x_1, ..., x_{b-1} are not used for computing the L1
		 * norm. Setting b (0 < b < N), one can protect variables, x_1, ..., x_{b-1}
		 * (e.g., a bias term of logistic regression) from being regularized. The
		 * default value is zero.
		 */
		private int orthantwiseStart = 0;

		/**
		 * End index for computing L1 norm of the variables. This parameter is valid
		 * only for OWL-QN method (i.e., \ref orthantwise_c != 0). This parameter e (0 <
		 * e <= N) specifies the index number at which the library stops computing the
		 * L1 norm of the variables x,
		 */
		private int orthantwiseEnd = -1;

		public int getM() {
			return m;
		}

		public void setM(int m) {
			this.m = m;
		}

		public double getEpsilon() {
			return epsilon;
		}

		public void setEpsilon(double epsilon) {
			this.epsilon = epsilon;
		}

		public int getPast() {
			return past;
		}

		public void setPast(int past) {
			this.past = past;
		}

		public double getDelta() {
			return delta;
		}

		public void setDelta(double delta) {
			this.delta = delta;
		}

		public int getMaxIterations() {
			return maxIterations;
		}

		public void setMaxIterations(int maxIterations) {
			this.maxIterations = maxIterations;
		}

		public LinesearchAlgorithm getLinesearch() {
			return linesearch;
		}

		public void setLinesearch(LinesearchAlgorithm linesearch) {
			this.linesearch = linesearch;
		}

		public int getMaxLinesearch() {
			return maxLinesearch;
		}

		public void setMaxLinesearch(int maxLinesearch) {
			this.maxLinesearch = maxLinesearch;
		}

		public double getMinStep() {
			return minStep;
		}

		public void setMinStep(double minStep) {
			this.minStep = minStep;
		}

		public double getMaxStep() {
			return maxStep;
		}

		public void setMaxStep(double maxStep) {
			this.maxStep = maxStep;
		}

		public double getFtol() {
			return ftol;
		}

		public void setFtol(double ftol) {
			this.ftol = ftol;
		}

		public double getWolfe() {
			return wolfe;
		}

		public void setWolfe(double wolfe) {
			this.wolfe = wolfe;
		}

		public double getGtol() {
			return gtol;
		}

		public void setGtol(double gtol) {
			this.gtol = gtol;
		}

		public double getXtol() {
			return xtol;
		}

		public void setXtol(double xtol) {
			this.xtol = xtol;
		}

		public double getOrthantwiseC() {
			return orthantwiseC;
		}

		public void setOrthantwiseC(double orthantwiseC) {
			this.orthantwiseC = orthantwiseC;
		}

		public int getOrthantwiseStart() {
			return orthantwiseStart;
		}

		public void setOrthantwiseStart(int orthantwiseStart) {
			this.orthantwiseStart = orthantwiseStart;
		}

		public int getOrthantwiseEnd() {
			return orthantwiseEnd;
		}

		public void setOrthantwiseEnd(int orthantwiseEnd) {
			this.orthantwiseEnd = orthantwiseEnd;
		}
}
