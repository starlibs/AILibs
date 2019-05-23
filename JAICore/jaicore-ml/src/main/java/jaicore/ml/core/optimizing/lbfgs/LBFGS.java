package jaicore.ml.core.optimizing.lbfgs;

/**
 * a port of liblbfgs to java http://www.chokkan.org/software/liblbfgs/
 *
 * 2017-04-15: Unit test by Nichole King is available at:
 * https://github.com/nking/curvature-scale-space-corners-and-transformations/blob/development/tests/thirdparty/brendano/LBFGS/LBFGSTest.java
 *
 * @mainpage libLBFGS: a library of Limited-memory
 *           Broyden-Fletcher-Goldfarb-Shanno (L-BFGS)
 * 
 * @section intro Introduction
 * 
 *          This library is a C port of the implementation of Limited-memory
 *          Broyden-Fletcher-Goldfarb-Shanno (L-BFGS) method written by Jorge
 *          Nocedal. The original FORTRAN source code is available at:
 *          http://www.ece.northwestern.edu/~nocedal/lbfgs.html
 * 
 *          The L-BFGS method solves the unconstrainted minimization problem,
 * 
 *          <pre>
    minimize F(x), x = (x1, x2, ..., xN),
 *          </pre>
 * 
 *          only if the objective function F(x) and its gradient G(x) are
 *          computable. The well-known Newton's method requires computation of
 *          the inverse of the hessian matrix of the objective function.
 *          However, the computational cost for the inverse hessian matrix is
 *          expensive especially when the objective function takes a large
 *          number of variables. The L-BFGS method iteratively finds a minimizer
 *          by approximating the inverse hessian matrix by information from last
 *          m iterations. This innovation saves the memory storage and
 *          computational time drastically for large-scaled problems.
 * 
 *          Among the various ports of L-BFGS, this library provides several
 *          features: - <b>Optimization with L1-norm (Orthant-Wise
 *          Limited-memory Quasi-Newton (OWL-QN) method)</b>: In addition to
 *          standard minimization problems, the library can minimize a function
 *          F(x) combined with L1-norm |x| of the variables, {F(x) + C |x|},
 *          where C is a constant scalar parameter. This feature is useful for
 *          estimating parameters of sparse log-linear models (e.g., logistic
 *          regression and maximum entropy) with L1-regularization (or Laplacian
 *          prior). - <b>Clean C code</b>: Unlike C codes generated
 *          automatically by f2c (Fortran 77 into C converter), this port
 *          includes changes based on my interpretations, improvements,
 *          optimizations, and clean-ups so that the ported code would be
 *          well-suited for a C code. In addition to comments inherited from the
 *          original code, a number of comments were added through my
 *          interpretations. - <b>Callback interface</b>: The library receives
 *          function and gradient values via a callback interface. The library
 *          also notifies the progress of the optimization by invoking a
 *          callback function. In the original implementation, a user had to set
 *          function and gradient values every time the function returns for
 *          obtaining updated values. - <b>Thread safe</b>: The library is
 *          thread-safe, which is the secondary gain from the callback
 *          interface. - <b>Cross platform.</b> The source code can be compiled
 *          on Microsoft Visual Studio 2010, GNU C Compiler (gcc), etc. -
 *          <b>Configurable precision</b>: A user can choose single-precision
 *          (float) or double-precision (double) accuracy by changing
 *          ::LBFGS_FLOAT macro. - <b>SSE/SSE2 optimization</b>: This library
 *          includes SSE/SSE2 optimization (written in compiler intrinsics) for
 *          vector arithmetic operations on Intel/AMD processors. The library
 *          uses SSE for float values and SSE2 for double values. The SSE/SSE2
 *          optimization routine is disabled by default.
 * 
 *          This library is used by: -
 *          <a href="http://www.chokkan.org/software/crfsuite/">CRFsuite: A fast
 *          implementation of Conditional Random Fields (CRFs)</a> -
 *          <a href="http://www.chokkan.org/software/classias/">Classias: A
 *          collection of machine-learning algorithms for classification</a> -
 *          <a href="http://www.public.iastate.edu/~gdancik/mlegp/">mlegp: an R
 *          package for maximum likelihood estimates for Gaussian processes</a>
 *          - <a href="http://infmath.uibk.ac.at/~matthiasf/imaging2/">imaging2:
 *          the imaging2 class library</a> - <a href=
 *          "http://search.cpan.org/~laye/Algorithm-LBFGS-0.16/">Algorithm::LBFGS
 *          - Perl extension for L-BFGS</a> -
 *          <a href="http://www.cs.kuleuven.be/~bernd/yap-lbfgs/">YAP-LBFGS (an
 *          interface to call libLBFGS from YAP Prolog)</a>
 * 
 * @section download Download
 * 
 *          - <a href=
 *          "https://github.com/downloads/chokkan/liblbfgs/liblbfgs-1.10.tar.gz">Source
 *          code</a> - <a href="https://github.com/chokkan/liblbfgs">GitHub
 *          repository</a>
 * 
 *          libLBFGS is distributed under the term of the
 *          <a href="http://opensource.org/licenses/mit-license.php">MIT
 *          license</a>.
 * 
 * @section changelog History - Version 1.10 (2010-12-22): - Fixed compiling
 *          errors on Mac OS X; this patch was kindly submitted by Nic
 *          Schraudolph. - Reduced compiling warnings on Mac OS X; this patch
 *          was kindly submitted by Tamas Nepusz. - Replaced memalign() with
 *          posix_memalign(). - Updated solution and project files for Microsoft
 *          Visual Studio 2010. - Version 1.9 (2010-01-29): - Fixed a mistake in
 *          checking the validity of the parameters "ftol" and "wolfe"; this was
 *          discovered by Kevin S. Van Horn. - Version 1.8 (2009-07-13): -
 *          Accepted the patch submitted by Takashi Imamichi; the backtracking
 *          method now has three criteria for choosing the step length: -
 *          ::LBFGS_LINESEARCH_BACKTRACKING_ARMIJO: sufficient decrease (Armijo)
 *          condition only - ::LBFGS_LINESEARCH_BACKTRACKING_WOLFE: regular
 *          Wolfe condition (sufficient decrease condition + curvature
 *          condition) - ::LBFGS_LINESEARCH_BACKTRACKING_STRONG_WOLFE: strong
 *          Wolfe condition - Updated the documentation to explain the above
 *          three criteria. - Version 1.7 (2009-02-28): - Improved OWL-QN
 *          routines for stability. - Removed the support of OWL-QN method in
 *          MoreThuente algorithm because it accidentally fails in early stages
 *          of iterations for some objectives. Because of this change, <b>the
 *          OW-LQN method must be used with the backtracking algorithm
 *          (::LBFGS_LINESEARCH_BACKTRACKING)</b>, or the library returns
 *          ::LBFGSERR_INVALID_LINESEARCH. - Renamed line search algorithms as
 *          follows: - ::LBFGS_LINESEARCH_BACKTRACKING: regular Wolfe condition.
 *          - ::LBFGS_LINESEARCH_BACKTRACKING_LOOSE: regular Wolfe condition. -
 *          ::LBFGS_LINESEARCH_BACKTRACKING_STRONG: strong Wolfe condition. -
 *          Source code clean-up. - Version 1.6 (2008-11-02): - Improved
 *          line-search algorithm with strong Wolfe condition, which was
 *          contributed by Takashi Imamichi. This routine is now default for
 *          ::LBFGS_LINESEARCH_BACKTRACKING. The previous line search algorithm
 *          with regular Wolfe condition is still available as
 *          ::LBFGS_LINESEARCH_BACKTRACKING_LOOSE. - Configurable stop index for
 *          L1-norm computation. A member variable
 *          ::lbfgs_parameter_t::orthantwise_end was added to specify the index
 *          number at which the library stops computing the L1 norm of the
 *          variables. This is useful to prevent some variables from being
 *          regularized by the OW-LQN method. - A sample program written in C++
 *          (sample/sample.cpp). - Version 1.5 (2008-07-10): - Configurable
 *          starting index for L1-norm computation. A member variable
 *          ::lbfgs_parameter_t::orthantwise_start was added to specify the
 *          index number from which the library computes the L1 norm of the
 *          variables. This is useful to prevent some variables from being
 *          regularized by the OWL-QN method. - Fixed a zero-division error when
 *          the initial variables have already been a minimizer (reported by
 *          Takashi Imamichi). In this case, the library returns
 *          ::LBFGS_ALREADY_MINIMIZED status code. - Defined ::LBFGS_SUCCESS
 *          status code as zero; removed unused constants, LBFGSFALSE and
 *          LBFGSTRUE. - Fixed a compile error in an implicit down-cast. -
 *          Version 1.4 (2008-04-25): - Configurable line search algorithms. A
 *          member variable ::lbfgs_parameter_t::linesearch was added to choose
 *          either MoreThuente method (::LBFGS_LINESEARCH_MORETHUENTE) or
 *          backtracking algorithm (::LBFGS_LINESEARCH_BACKTRACKING). - Fixed a
 *          bug: the previous version did not compute psuedo-gradients properly
 *          in the line search routines for OWL-QN. This bug might quit an
 *          iteration process too early when the OWL-QN routine was activated (0
 *          < ::lbfgs_parameter_t::orthantwise_c). - Configure script for POSIX
 *          environments. - SSE/SSE2 optimizations with GCC. - New functions
 *          ::lbfgs_malloc and ::lbfgs_free to use SSE/SSE2 routines
 *          transparently. It is uncessary to use these functions for libLBFGS
 *          built without SSE/SSE2 routines; you can still use any memory
 *          allocators if SSE/SSE2 routines are disabled in libLBFGS. - Version
 *          1.3 (2007-12-16): - An API change. An argument was added to lbfgs()
 *          function to receive the final value of the objective function. This
 *          argument can be set to \c NULL if the final value is unnecessary. -
 *          Fixed a null-pointer bug in the sample code (reported by Takashi
 *          Imamichi). - Added build scripts for Microsoft Visual Studio 2005
 *          and GCC. - Added README file. - Version 1.2 (2007-12-13): - Fixed a
 *          serious bug in orthant-wise L-BFGS. An important variable was used
 *          without initialization. - Version 1.1 (2007-12-01): - Implemented
 *          orthant-wise L-BFGS. - Implemented lbfgs_parameter_init() function.
 *          - Fixed several bugs. - API documentation. - Version 1.0
 *          (2007-09-20): - Initial release.
 * 
 * @section api Documentation
 * 
 *          - @ref liblbfgs_api "libLBFGS API"
 * 
 * @section sample Sample code
 * 
 * @include sample.c
 * 
 * @section ack Acknowledgements
 * 
 *          The L-BFGS algorithm is described in: - Jorge Nocedal. Updating
 *          Quasi-Newton Matrices with Limited Storage. <i>Mathematics of
 *          Computation</i>, Vol. 35, No. 151, pp. 773--782, 1980. - Dong C. Liu
 *          and Jorge Nocedal. On the limited memory BFGS method for large scale
 *          optimization. <i>Mathematical Programming</i> B, Vol. 45, No. 3, pp.
 *          503-528, 1989.
 * 
 *          The line search algorithms used in this implementation are described
 *          in: - John E. Dennis and Robert B. Schnabel. <i>Numerical Methods
 *          for Unconstrained Optimization and Nonlinear Equations</i>,
 *          Englewood Cliffs, 1983. - Jorge J. More and David J. Thuente. Line
 *          search algorithm with guaranteed sufficient decrease. <i>ACM
 *          Transactions on Mathematical Software (TOMS)</i>, Vol. 20, No. 3,
 *          pp. 286-307, 1994.
 * 
 *          This library also implements Orthant-Wise Limited-memory
 *          Quasi-Newton (OWL-QN) method presented in: - Galen Andrew and
 *          Jianfeng Gao. Scalable training of L1-regularized log-linear models.
 *          In <i>Proceedings of the 24th International Conference on Machine
 *          Learning (ICML 2007)</i>, pp. 33-40, 2007.
 * 
 *          Special thanks go to: - Yoshimasa Tsuruoka and Daisuke Okanohara for
 *          technical information about OWL-QN - Takashi Imamichi for the useful
 *          enhancements of the backtracking method - Kevin S. Van Horn, Nic
 *          Schraudolph, and Tamas Nepusz for bug fixes
 * 
 *          Finally I would like to thank the original author, Jorge Nocedal,
 *          who has been distributing the effieicnt and explanatory
 *          implementation in an open source licence.
 * 
 * @section reference Reference
 * 
 *          - <a href=
 *          "http://www.ece.northwestern.edu/~nocedal/lbfgs.html">L-BFGS</a> by
 *          Jorge Nocedal. - <a href=
 *          "http://research.microsoft.com/en-us/downloads/b1eb1016-1738-4bd5-83a9-370c9d498a03/default.aspx">Orthant-Wise
 *          Limited-memory Quasi-Newton Optimizer for L1-regularized
 *          Objectives</a> by Galen Andrew. -
 *          <a href="http://chasen.org/~taku/software/misc/lbfgs/">C port (via
 *          f2c)</a> by Taku Kudo. - <a href=
 *          "http://www.alglib.net/optimization/lbfgs.php">C#/C++/Delphi/VisualBasic6
 *          port</a> in ALGLIB. -
 *          <a href="http://cctbx.sourceforge.net/">Computational
 *          Crystallography Toolbox</a> includes <a href=
 *          "http://cctbx.sourceforge.net/current_cvs/c_plus_plus/namespacescitbx_1_1lbfgs.html">scitbx::lbfgs</a>.
 */
public class LBFGS {

	/**
	 * Return values of lbfgs().
	 * 
	 * Roughly speaking, a negative value indicates an error.
	 */
	public enum Status {
		/** L-BFGS reaches convergence. */
		LBFGS_SUCCESS, LBFGS_STOP,
		/** The initial variables already minimize the objective function. */
		LBFGS_ALREADY_MINIMIZED,

		/** Unknown error. */
		LBFGSERR_UNKNOWNERROR,
		/** Logic error. */
		LBFGSERR_LOGICERROR,
		/** Insufficient memory. */
		LBFGSERR_OUTOFMEMORY,
		/** The minimization process has been canceled. */
		LBFGSERR_CANCELED,
		/** Invalid number of variables specified. */
		LBFGSERR_INVALID_N,
		/** Invalid number of variables (for SSE) specified. */
		LBFGSERR_INVALID_N_SSE,
		/** The array x must be aligned to 16 (for SSE). */
		LBFGSERR_INVALID_X_SSE,
		/** Invalid parameter lbfgs_parameter_t::epsilon specified. */
		LBFGSERR_INVALID_EPSILON,
		/** Invalid parameter lbfgs_parameter_t::past specified. */
		LBFGSERR_INVALID_TESTPERIOD,
		/** Invalid parameter lbfgs_parameter_t::delta specified. */
		LBFGSERR_INVALID_DELTA,
		/** Invalid parameter lbfgs_parameter_t::linesearch specified. */
		LBFGSERR_INVALID_LINESEARCH,
		/** Invalid parameter lbfgs_parameter_t::max_step specified. */
		LBFGSERR_INVALID_MINSTEP,
		/** Invalid parameter lbfgs_parameter_t::max_step specified. */
		LBFGSERR_INVALID_MAXSTEP,
		/** Invalid parameter lbfgs_parameter_t::ftol specified. */
		LBFGSERR_INVALID_FTOL,
		/** Invalid parameter lbfgs_parameter_t::wolfe specified. */
		LBFGSERR_INVALID_WOLFE,
		/** Invalid parameter lbfgs_parameter_t::gtol specified. */
		LBFGSERR_INVALID_GTOL,
		/** Invalid parameter lbfgs_parameter_t::xtol specified. */
		LBFGSERR_INVALID_XTOL,
		/** Invalid parameter lbfgs_parameter_t::max_linesearch specified. */
		LBFGSERR_INVALID_MAXLINESEARCH,
		/** Invalid parameter lbfgs_parameter_t::orthantwise_c specified. */
		LBFGSERR_INVALID_ORTHANTWISE,
		/** Invalid parameter lbfgs_parameter_t::orthantwise_start specified. */
		LBFGSERR_INVALID_ORTHANTWISE_START,
		/** Invalid parameter lbfgs_parameter_t::orthantwise_end specified. */
		LBFGSERR_INVALID_ORTHANTWISE_END,
		/** The line-search step went out of the interval of uncertainty. */
		LBFGSERR_OUTOFINTERVAL,
		/**
		 * A logic error occurred; alternatively, the interval of uncertainty became too
		 * small.
		 */
		LBFGSERR_INCORRECT_TMINMAX,
		/**
		 * A rounding error occurred; alternatively, no line-search step satisfies the
		 * sufficient decrease and curvature conditions.
		 */
		LBFGSERR_ROUNDING_ERROR,
		/** The line-search step became smaller than lbfgs_parameter_t::min_step. */
		LBFGSERR_MINIMUMSTEP,
		/** The line-search step became larger than lbfgs_parameter_t::max_step. */
		LBFGSERR_MAXIMUMSTEP,
		/** The line-search routine reaches the maximum number of evaluations. */
		LBFGSERR_MAXIMUMLINESEARCH,
		/** The algorithm routine reaches the maximum number of iterations. */
		LBFGSERR_MAXIMUMITERATION,
		/**
		 * Relative width of the interval of uncertainty is at most
		 * lbfgs_parameter_t::xtol.
		 */
		LBFGSERR_WIDTHTOOSMALL,
		/** A logic error (negative line-search step) occurred. */
		LBFGSERR_INVALIDPARAMETERS,
		/** The current search direction increases the objective function value. */
		LBFGSERR_INCREASEGRADIENT;

		public boolean hasConverged() {
			return this == LBFGS_SUCCESS || this == LBFGS_STOP;
		}

		public boolean isError() {
			return this != LBFGS_SUCCESS && this != LBFGS_STOP && this != LBFGS_ALREADY_MINIMIZED;
		}
	}

	/**
	 * Line search algorithms.
	 */
	enum LinesearchAlgorithm {

		/** MoreThuente method proposd by More and Thuente. */
		LBFGS_LINESEARCH_MORETHUENTE,

		/**
		 * Backtracking method with the Armijo condition. The backtracking method finds
		 * the step length such that it satisfies the sufficient decrease (Armijo)
		 * condition, - f(x + a * d) <= f(x) + lbfgs_parameter_t::ftol * a * g(x)^T d,
		 *
		 * where x is the current point, d is the current search direction, and a is the
		 * step length.
		 */
		LBFGS_LINESEARCH_BACKTRACKING_ARMIJO,

		/**
		 * Backtracking method with regular Wolfe condition. The backtracking method
		 * finds the step length such that it satisfies both the Armijo condition
		 * (LBFGS_LINESEARCH_BACKTRACKING_ARMIJO) and the curvature condition, - g(x + a
		 * * d)^T d >= lbfgs_parameter_t::wolfe * g(x)^T d,
		 *
		 * where x is the current point, d is the current search direction, and a is the
		 * step length.
		 */
		LBFGS_LINESEARCH_BACKTRACKING_WOLFE,

		/**
		 * Backtracking method with strong Wolfe condition. The backtracking method
		 * finds the step length such that it satisfies both the Armijo condition
		 * (LBFGS_LINESEARCH_BACKTRACKING_ARMIJO) and the following condition, - |g(x +
		 * a * d)^T d| <= lbfgs_parameter_t::wolfe * |g(x)^T d|,
		 *
		 * where x is the current point, d is the current search direction, and a is the
		 * step length.
		 */
		LBFGS_LINESEARCH_BACKTRACKING_STRONG_WOLFE,
	}

	static interface LineSearchProc {
		public Status go(int n, double[] x, double[] f, double[] g, double[] s, double[] stp, final double[] xp,
				final double[] gp, double[] wa, CallbackDataT cd, LBFGSParameters param);
	}

	/**
	 * Callback interface to provide objective function and gradient evaluations.
	 * [BTO: liblbfgs's wants both at once. this is smart: often this halves the
	 * cost; e.g. evaluate partitions only once for loglinear gradients.]
	 *
	 * The lbfgs() function call this function to obtain the values of objective
	 * function and its gradients when needed. A client program must implement this
	 * function to evaluate the values of the objective function and its gradients,
	 * given current values of variables.
	 * 
	 * Protocol: receive x, fill in g, return objective.
	 * 
	 * @param x
	 *            The current values of variables.
	 * @param g
	 *            The gradient vector. The callback function must compute the
	 *            gradient values for the current variables.
	 * @param n
	 *            The number of variables.
	 * @param step
	 *            The current step of the line search routine.
	 * @retval The value of the objective function for the current variables.
	 */
	public static interface Function {
		/** receive x. fill in g. return objective. */
		public double evaluate(final double[] x, double[] g, int n, double step);
	}

	/*
	 * A user must implement a function compatible with ::lbfgs_evaluate_t
	 * (evaluation callback) and pass the pointer to the callback function to
	 * lbfgs() arguments. Similarly, a user can implement a function compatible with
	 * ::lbfgs_progress_t (progress callback) to obtain the current progress (e.g.,
	 * variables, function value, ||G||, etc) and to cancel the iteration process if
	 * necessary. Implementation of a progress callback is optional: a user can pass
	 * \c NULL if progress notification is not necessary.
	 * 
	 * In addition, a user must preserve two requirements: - The number of variables
	 * must be multiples of 16 (this is not 4). - The memory block of variable array
	 * ::x must be aligned to 16.
	 * 
	 * This algorithm terminates an optimization when:
	 * 
	 * ||G|| < \epsilon \cdot \max(1, ||x||) .
	 * 
	 * In this formula, ||.|| denotes the Euclidean norm.
	 */

	public static class Result {
		private Status status;
		private int additionalStatus;
		private double objective = Double.MAX_VALUE;

		public Result(Status s) {
			status = s;
		}

		public String toString() {
			return String.format("status=%s obj=%g", status, objective);
		}

		public Status getStatus() {
			return status;
		}

		public void setStatus(Status status) {
			this.status = status;
		}

		public int getAdditionalStatus() {
			return additionalStatus;
		}

		public void setAdditionalStatus(int additionalStatus) {
			this.additionalStatus = additionalStatus;
		}

		public double getObjective() {
			return objective;
		}

		public void setObjective(double objective) {
			this.objective = objective;
		}
	}

	static double max3(double a, double b, double c) {
		return Math.max(Math.max(a, b), c);
	}

	static class CallbackDataT {
		int n;
		Function procEvaluate;
	}

	static class IterationDataT {
		double alpha;
		// BTO tricky, i think these were aliased pointers or something?
		double[] s; /* [n] */
		double[] y; /* [n] */
		double ys; /* LFBGSArrayUtils.vecdot(y, s) */
	}

	/**
	 * Use default parameters. See
	 * {@link LBFGS#lfbgs(double[], Function, ProgressCallback, LBFGSParameters)}
	 */
	public static Result lbfgs(double[] init, Function procEvaluate) {
		return lbfgs(init, procEvaluate, new LBFGSParameters());
	}

	/**
	 * Use debug-friendly parameters & callback. See
	 * {@link LBFGS#lfbgs(double[], Function, ProgressCallback, LBFGSParameters)}
	 */
	public static Result lbfgs(double[] init, int maxIter, Function procEvaluate) {
		LBFGSParameters p = new LBFGSParameters();
		p.setMaxIterations(maxIter);
		return lbfgs(init, procEvaluate, p);
	}

	/**
	 * Start a L-BFGS optimization.
	 *
	 * @param n
	 *            The number of variables.
	 * @param x
	 *            The array of variables. A client program can set default values
	 *            for the optimization and receive the optimization result through
	 *            this array.
	 * @param procEvaluate
	 *            The callback function to provide function and gradient evaluations
	 *            given a current values of variables. A client program must
	 *            implement a callback function compatible with \ref
	 *            lbfgs_evaluate_t and pass the pointer to the callback function.
	 * @param proc_progress
	 *            The callback function to receive the progress (the number of
	 *            iterations, the current value of the objective function) of the
	 *            minimization process. This argument can be set to \c NULL if a
	 *            progress report is unnecessary.
	 * @param param
	 *            The pointer to a structure representing parameters for L-BFGS
	 *            optimization. A client program can set this parameter to NULL to
	 *            use the default parameters.
	 * @retval Result The status code and final objective.
	 */
	public static Result lbfgs(double[] x, Function procEvaluate, LBFGSParameters param) {
		int n = x.length;

		Result ret = new Result(null);
		int i;
		int j;
		int k;
		int end;
		int bound;
		Status ls;
		double[] step = new double[] { 0 };

		/* Constant parameters and their default values. */
		final int m = param.getM();

		double[] xp;
		double[] g;
		double[] gp;
		double[] pg;
		double[] d;
		double[] w;
		double[] pf = null;
		IterationDataT[] lm;
		IterationDataT it;
		double ys;
		double yy;
		double xnorm;
		double gnorm;
		double beta;
		double[] fx = new double[] { 0 }; // singleton passing to linesearch
		double rate = 0;
		LineSearchProc linesearch;

		/* Construct a callback data. */
		CallbackDataT cd = new CallbackDataT();
		cd.n = n;
		cd.procEvaluate = procEvaluate;

		/* Check the input parameters for errors. */
		if (n <= 0) {
			return new Result(Status.LBFGSERR_INVALID_N);
		}
		if (param.getEpsilon() < 0.) {
			return new Result(Status.LBFGSERR_INVALID_EPSILON);
		}
		if (param.getPast() < 0) {
			return new Result(Status.LBFGSERR_INVALID_TESTPERIOD);
		}
		if (param.getDelta() < 0.) {
			return new Result(Status.LBFGSERR_INVALID_DELTA);
		}
		if (param.getMinStep() < 0.) {
			return new Result(Status.LBFGSERR_INVALID_MINSTEP);
		}
		if (param.getMaxStep() < param.getMinStep()) {
			return new Result(Status.LBFGSERR_INVALID_MAXSTEP);
		}
		if (param.getFtol() < 0.) {
			return new Result(Status.LBFGSERR_INVALID_FTOL);
		}
		if ((param.linesearch == LinesearchAlgorithm.LBFGS_LINESEARCH_BACKTRACKING_WOLFE
				|| param.linesearch == LinesearchAlgorithm.LBFGS_LINESEARCH_BACKTRACKING_STRONG_WOLFE)
				&& (param.getWolfe() <= param.getFtol() || 1. <= param.getWolfe())) {
			return new Result(Status.LBFGSERR_INVALID_WOLFE);
		}
		if (param.getGtol() < 0.) {
			return new Result(Status.LBFGSERR_INVALID_GTOL);
		}
		if (param.getXtol() < 0.) {
			return new Result(Status.LBFGSERR_INVALID_XTOL);
		}
		if (param.getMaxLinesearch() <= 0) {
			return new Result(Status.LBFGSERR_INVALID_MAXLINESEARCH);
		}
		if (param.getOrthantwiseC() < 0.) {
			return new Result(Status.LBFGSERR_INVALID_ORTHANTWISE);
		}
		if (param.getOrthantwiseStart() < 0 || n < param.getOrthantwiseStart()) {
			return new Result(Status.LBFGSERR_INVALID_ORTHANTWISE_START);
		}
		if (param.getOrthantwiseEnd() < 0) {
			param.setOrthantwiseEnd(n);
		}
		if (n < param.getOrthantwiseEnd()) {
			return new Result(Status.LBFGSERR_INVALID_ORTHANTWISE_END);
		}
		if (param.getOrthantwiseC() != 0.) {
			/* Only the backtracking method is available. */
			linesearch = new LineSearchBacktrackingOwlqn();
		} else {
			switch (param.linesearch) {
			case LBFGS_LINESEARCH_MORETHUENTE:
				linesearch = new LineSearchMorethuente();
				break;
			case LBFGS_LINESEARCH_BACKTRACKING_ARMIJO:
			case LBFGS_LINESEARCH_BACKTRACKING_WOLFE:
			case LBFGS_LINESEARCH_BACKTRACKING_STRONG_WOLFE:
				linesearch = new LineSearchBacktracking();
				break;
			default:
				return new Result(Status.LBFGSERR_INVALID_LINESEARCH);
			}
		}

		/* Allocate working space. */
		xp = new double[n];
		g = new double[n];
		gp = new double[n];
		d = new double[n];
		w = new double[n];

		if (param.getOrthantwiseC() != 0) {
			/* Allocate working space for OW-LQN. */
			pg = new double[n];
		} else {
			pg = new double[0]; // to make java compiler happy
		}

		/* Allocate limited memory storage. */
		lm = new IterationDataT[m];

		/* Initialize the limited memory. */
		for (i = 0; i < m; ++i) {
			lm[i] = new IterationDataT();
			it = lm[i];
			it.alpha = 0;
			it.ys = 0;
			it.s = new double[n];
			it.y = new double[n];
		}

		/* Allocate an array for storing previous values of the objective function. */
		if (0 < param.getPast()) {
			pf = new double[param.getPast()];
		}

		/* Evaluate the function value and its gradient. */
		fx[0] = cd.procEvaluate.evaluate(x, g, cd.n, 0);
		if (0. != param.getOrthantwiseC()) {
			/* Compute the L1 norm of the variable and add it to the object value. */
			xnorm = owlqnX1norm(x, param.getOrthantwiseStart(), param.getOrthantwiseEnd());
			fx[0] += xnorm * param.getOrthantwiseC();
			owlqnPseudoGradient(pg, x, g, n, param.getOrthantwiseC(), param.getOrthantwiseStart(), param.getOrthantwiseEnd());
		}

		/* Store the initial value of the objective function. */
		if (pf != null) {
			pf[0] = fx[0];
		}

		/*
		 * Compute the direction; we assume the initial hessian matrix H_0 as the
		 * identity matrix.
		 */
		if (param.getOrthantwiseC() == 0.) {
			LBFGSArrayUtils.vecncpy(d, g, n);
		} else {
			LBFGSArrayUtils.vecncpy(d, pg, n);
		}

		/*
		 * Make sure that the initial variables are not a minimizer.
		 */
		xnorm = LBFGSArrayUtils.vec2norm(x);
		if (param.getOrthantwiseC() == 0.) {
			gnorm = LBFGSArrayUtils.vec2norm(g);
		} else {
			gnorm = LBFGSArrayUtils.vec2norm(pg);
		}
		if (xnorm < 1.0)
			xnorm = 1.0;
		if (gnorm / xnorm <= param.getEpsilon()) {
			return new Result(Status.LBFGS_ALREADY_MINIMIZED);
		}

		/*
		 * Compute the initial step: step = 1.0 / sqrt(LFBGSArrayUtils.vecdot(d, d, n))
		 */
		step[0] = LBFGSArrayUtils.vec2norminv(d);

		k = 1;
		end = 0;
		for (;;) {
			/* Store the current position and gradient vectors. */
			LBFGSArrayUtils.veccpy(xp, x, n);
			LBFGSArrayUtils.veccpy(gp, g, n);

			/* Search for an optimal step. */
			if (param.getOrthantwiseC() == 0.) {
				ls = linesearch.go(n, x, fx, g, d, step, xp, gp, w, cd, param);
			} else {
				ls = linesearch.go(n, x, fx, g, d, step, xp, pg, w, cd, param);
				owlqnPseudoGradient(pg, x, g, n, param.getOrthantwiseC(), param.getOrthantwiseStart(), param.getOrthantwiseEnd());
			}
			if (ls != null && ls.isError()) {
				/* Revert to the previous point. */
				LBFGSArrayUtils.veccpy(x, xp, n);
				LBFGSArrayUtils.veccpy(g, gp, n);
				ret.status = ls;
				ret.objective = fx[0];
				return ret;
			}

			/* Compute x and g norms. */
			xnorm = LBFGSArrayUtils.vec2norm(x);
			if (param.getOrthantwiseC() == 0.) {
				gnorm = LBFGSArrayUtils.vec2norm(g);
			} else {
				gnorm = LBFGSArrayUtils.vec2norm(pg);
			}

			/*
			 * Convergence test. The criterion is given by the following formula: |g(x)| /
			 * \max(1, |x|) < \epsilon
			 */
			if (xnorm < 1.0)
				xnorm = 1.0;
			if (gnorm / xnorm <= param.getEpsilon()) {
				/* Convergence. */
				ret.status = Status.LBFGS_SUCCESS;
				break;
			}

			/*
			 * Test for stopping criterion. The criterion is given by the following formula:
			 * (f(past_x) - f(x)) / f(x) < \delta
			 */
			if (pf != null) {
				/* We don't test the stopping criterion while k < past. */
				if (param.getPast() <= k) {
					/* Compute the relative improvement from the past. */
					rate = (pf[k % param.getPast()] - fx[0]) / fx[0];

					/* The stopping criterion. */
					if (rate < param.getDelta()) {
						ret.status = Status.LBFGS_STOP;
						break;
					}
				}

				/* Store the current value of the objective function. */
				pf[k % param.getPast()] = fx[0];
			}

			if (param.getMaxIterations() != 0 && param.getMaxIterations() < k + 1) {
				/* Maximum number of iterations. */
				ret.status = Status.LBFGSERR_MAXIMUMITERATION;
				break;
			}

			/*
			 * Update vectors s and y: s_{k+1} = x_{k+1} - x_{k} = \step * d_{k}. y_{k+1} =
			 * g_{k+1} - g_{k}.
			 */
			it = lm[end];
			LBFGSArrayUtils.vecdiff(it.s, x, xp, n);
			LBFGSArrayUtils.vecdiff(it.y, g, gp, n);

			/*
			 * Compute scalars ys and yy: ys = y^t \cdot s = 1 / \rho. yy = y^t \cdot y.
			 * Notice that yy is used for scaling the hessian matrix H_0 (Cholesky factor).
			 */
			ys = LBFGSArrayUtils.vecdot(it.y, it.s);
			yy = LBFGSArrayUtils.vecdot(it.y, it.y);
			it.ys = ys;

			/*
			 * Recursive formula to compute dir = -(H \cdot g). This is described in page
			 * 779 of: Jorge Nocedal. Updating Quasi-Newton Matrices with Limited Storage.
			 * Mathematics of Computation, Vol. 35, No. 151, pp. 773--782, 1980.
			 */
			bound = (m <= k) ? m : k;
			++k;
			end = (end + 1) % m;

			/* Compute the steepest direction. */
			if (param.getOrthantwiseC() == 0.) {
				/* Compute the negative of gradients. */
				LBFGSArrayUtils.vecncpy(d, g, n);
			} else {
				LBFGSArrayUtils.vecncpy(d, pg, n);
			}

			j = end;
			for (i = 0; i < bound; ++i) {
				j = (j + m - 1) % m;
				it = lm[j];
				/* \alpha_{j} = \rho_{j} s^{t}_{j} \cdot q_{k+1}. */
				it.alpha = LBFGSArrayUtils.vecdot(it.s, d);
				it.alpha /= it.ys;
				/* q_{i} = q_{i+1} - \alpha_{i} y_{i}. */
				LBFGSArrayUtils.vecadd(d, it.y, -it.alpha, n);
			}

			LBFGSArrayUtils.vecscale(d, ys / yy, n);

			for (i = 0; i < bound; ++i) {
				it = lm[j];
				/* \beta_{j} = \rho_{j} y^t_{j} \cdot \gamma_{i}. */
				beta = LBFGSArrayUtils.vecdot(it.y, d);
				beta /= it.ys;
				/* \gamma_{i+1} = \gamma_{i} + (\alpha_{j} - \beta_{j}) s_{j}. */
				LBFGSArrayUtils.vecadd(d, it.s, it.alpha - beta, n);
				j = (j + 1) % m;
			}

			/*
			 * Constrain the search direction for orthant-wise updates.
			 */
			if (param.getOrthantwiseC() != 0.) {
				for (i = param.getOrthantwiseStart(); i < param.getOrthantwiseEnd(); ++i) {
					if (d[i] * pg[i] >= 0) {
						d[i] = 0;
					}
				}
			}

			/*
			 * Now the search direction d is ready. We try step = 1 first.
			 */
			step[0] = 1.0;
		}

		ret.objective = fx[0];
		return ret;
	}

	static class LineSearchBacktracking implements LineSearchProc {

		public Status go(int n, double[] x, double[] f, double[] g, double[] s, double[] stp, // BTO: um i think this is
																								// supposed to be a
																								// singleton
				final double[] xp, final double[] gp, double[] wp, CallbackDataT cd, LBFGSParameters param) {
			int count = 0;
			double width;
			double dg;
			double finit = 0;
			double dginit;
			double dgtest;
			final double dec = 0.5;
			final double inc = 2.1;

			/* Check the input parameters for errors. */
			if (stp[0] <= 0.) {
				return Status.LBFGSERR_INVALIDPARAMETERS;
			}

			/* Compute the initial gradient in the search direction. */
			dginit = LBFGSArrayUtils.vecdot(g, s);

			/* Make sure that s points to a descent direction. */
			if (0 < dginit) {
				return Status.LBFGSERR_INCREASEGRADIENT;
			}

			/* The initial value of the objective function. */
			finit = f[0];
			dgtest = param.getFtol() * dginit;

			for (;;) {
				LBFGSArrayUtils.veccpy(x, xp, n);
				LBFGSArrayUtils.vecadd(x, s, stp[0], n);

				/* Evaluate the function and gradient values. */
				f[0] = cd.procEvaluate.evaluate(x, g, n, stp[0]);

				++count;

				if (f[0] > finit + stp[0] * dgtest) {
					width = dec;
				} else {
					/* The sufficient decrease condition (Armijo condition). */
					if (param.linesearch == LinesearchAlgorithm.LBFGS_LINESEARCH_BACKTRACKING_ARMIJO) {
						/* Exit with the Armijo condition. */
						return Status.LBFGS_SUCCESS; // BTO changed

					}

					/* Check the Wolfe condition. */
					dg = LBFGSArrayUtils.vecdot(g, s);
					if (dg < param.getWolfe() * dginit) {
						width = inc;
					} else {
						if (param.linesearch == LinesearchAlgorithm.LBFGS_LINESEARCH_BACKTRACKING_WOLFE) {
							/* Exit with the regular Wolfe condition. */
							return Status.LBFGS_SUCCESS; // BTO changed
						}

						/* Check the strong Wolfe condition. */
						if (dg > -param.getWolfe() * dginit) {
							width = dec;
						} else {
							/* Exit with the strong Wolfe condition. */
							return Status.LBFGS_SUCCESS; // BTO changed
						}
					}
				}

				if (stp[0] < param.getMinStep()) {
					/* The step is the minimum value. */
					return Status.LBFGSERR_MINIMUMSTEP;
				}
				if (stp[0] > param.getMaxStep()) {
					/* The step is the maximum value. */
					return Status.LBFGSERR_MAXIMUMSTEP;
				}
				if (param.getMaxLinesearch() <= count) {
					/* Maximum number of iteration. */
					return Status.LBFGSERR_MAXIMUMLINESEARCH;
				}

				stp[0] *= width;
			}
		}
	}

	static class LineSearchBacktrackingOwlqn implements LineSearchProc {

		public Status go(int n, double[] x, double[] f, double[] g, double[] s, double[] stp, final double[] xp,
				final double[] gp, double[] wp, CallbackDataT cd, LBFGSParameters param) {
			int i = 0;
			int count = 0;
			double width = 0.5;
			double norm;
			double finit = f[0];
			double dgtest;

			/* Check the input parameters for errors. */
			if (stp[0] <= 0.) {
				return Status.LBFGSERR_INVALIDPARAMETERS;
			}

			/* Choose the orthant for the new point. */
			for (i = 0; i < n; ++i) {
				wp[i] = (xp[i] == 0.) ? -gp[i] : xp[i];
			}

			for (;;) {
				/* Update the current point. */
				LBFGSArrayUtils.veccpy(x, xp, n);
				LBFGSArrayUtils.vecadd(x, s, stp[0], n);

				/* The current point is projected onto the orthant. */
				owlqnProject(x, wp, param.getOrthantwiseStart(), param.getOrthantwiseEnd());

				/* Evaluate the function and gradient values. */
				f[0] = cd.procEvaluate.evaluate(x, g, cd.n, stp[0]);

				/* Compute the L1 norm of the variables and add it to the object value. */
				norm = owlqnX1norm(x, param.getOrthantwiseStart(), param.getOrthantwiseEnd());
				f[0] += norm * param.getOrthantwiseC();

				++count;

				dgtest = 0.;
				for (i = 0; i < n; ++i) {
					dgtest += (x[i] - xp[i]) * gp[i];
				}

				if (f[0] <= finit + param.getFtol() * dgtest) {
					/* The sufficient decrease condition. */
					return Status.LBFGS_SUCCESS; // BTO changed
				}

				if (stp[0] < param.getMinStep()) {
					/* The step is the minimum value. */
					return Status.LBFGSERR_MINIMUMSTEP;
				}
				if (stp[0] > param.getMaxStep()) {
					/* The step is the maximum value. */
					return Status.LBFGSERR_MAXIMUMSTEP;
				}
				if (param.getMaxLinesearch() <= count) {
					/* Maximum number of iteration. */
					return Status.LBFGSERR_MAXIMUMLINESEARCH;
				}

				stp[0] *= width;
			}
		}
	}

	static class LineSearchMorethuente implements LineSearchProc {

		@Override
		public Status go(int n, double[] x, double[] f, double[] g, double[] s, double[] stp, double[] xp, double[] gp,
				double[] wa, CallbackDataT cd, LBFGSParameters param) {
			assert false : "unimplemented";
			return null;
		}
	}

	/**
	 * Find a minimizer of an interpolated cubic function.
	 * 
	 * @param cm
	 *            The minimizer of the interpolated cubic. BTO REMOVED, return
	 *            instead
	 * @param u
	 *            The value of one point, u.
	 * @param fu
	 *            The value of f(u).
	 * @param du
	 *            The value of f'(u).
	 * @param v
	 *            The value of another point, v.
	 * @param fv
	 *            The value of f(v).
	 * @param du
	 *            The value of f'(v).
	 */
	static double cubicMinimizer(double u, double fu, double du, double v, double fv, double dv) {
		// #define CUBIC_MINIMIZER(cm, u, fu, du, v, fv, dv) \
		double d = (v) - (u);
		double theta = ((fu) - (fv)) * 3 / d + (du) + (dv);
		double p = Math.abs(theta);
		double q = Math.abs(du);
		double r = Math.abs(dv);
		double s = max3(p, q, r);
		/* gamma = s*sqrt((theta/s)**2 - (du/s) * (dv/s)) */
		double a = theta / s;
		double gamma = s * Math.sqrt(a * a - ((du) / s) * ((dv) / s));
		if ((v) < (u))
			gamma = -gamma;
		p = gamma - (du) + theta;
		q = gamma - (du) + gamma + (dv);
		r = p / q;
		return (u) + r * d;
	}

	/**
	 * Find a minimizer of an interpolated cubic function.
	 * 
	 * @param cm
	 *            The minimizer of the interpolated cubic.
	 * @param u
	 *            The value of one point, u.
	 * @param fu
	 *            The value of f(u).
	 * @param du
	 *            The value of f'(u).
	 * @param v
	 *            The value of another point, v.
	 * @param fv
	 *            The value of f(v).
	 * @param du
	 *            The value of f'(v).
	 * @param xmin
	 *            The maximum value.
	 * @param xmin
	 *            The minimum value.
	 */
	// #define CUBIC_MINIMIZER2(cm, u, fu, du, v, fv, dv, xmin, xmax) \
	static double cubicMinimizer2(double u, double fu, double du, double v, double fv, double dv, double xmin,
			double xmax) {
		double d = (v) - (u);
		double theta = ((fu) - (fv)) * 3 / d + (du) + (dv);
		double p = Math.abs(theta);
		double q = Math.abs(du);
		double r = Math.abs(dv);
		double s = max3(p, q, r);
		/* gamma = s*sqrt((theta/s)**2 - (du/s) * (dv/s)) */
		double a = theta / s;
		double gamma = s * Math.sqrt(Math.max(0, a * a - ((du) / s) * ((dv) / s)));
		if ((u) < (v))
			gamma = -gamma;
		p = gamma - (dv) + theta;
		q = gamma - (dv) + gamma + (du);
		r = p / q;
		double cm;
		if (r < 0. && gamma != 0.) {
			(cm) = (v) - r * d;
		} else if (a < 0) {
			(cm) = (xmax);
		} else {
			(cm) = (xmin);
		}
		return cm;
	}

	/**
	 * Find a minimizer of an interpolated quadratic function.
	 * 
	 * @return The minimizer of the interpolated quadratic.
	 * @param u
	 *            The value of one point, u.
	 * @param fu
	 *            The value of f(u).
	 * @param du
	 *            The value of f'(u).
	 * @param v
	 *            The value of another point, v.
	 * @param fv
	 *            The value of f(v).
	 */
	static double quardMinimizer(double u, double fu, double du, double v, double fv) {
		// #define QUARD_MINIMIZER(qm, u, fu, du, v, fv) \
		double a = (v) - (u);
		return (u) + (du) / (((fu) - (fv)) / a + (du)) / 2 * a;
	}

	/**
	 * Find a minimizer of an interpolated quadratic function.
	 * 
	 * @param qm
	 *            The minimizer of the interpolated quadratic.
	 * @param u
	 *            The value of one point, u.
	 * @param du
	 *            The value of f'(u).
	 * @param v
	 *            The value of another point, v.
	 * @param dv
	 *            The value of f'(v).
	 */
	// #define QUARD_MINIMIZER2(qm, u, du, v, dv) \
	static double quardMinimizer2(double u, double du, double v, double dv) {
		double a = (u) - (v);
		return (v) + (dv) / ((dv) - (du)) * a;
	}

	static double owlqnX1norm(final double[] x, final int start, final int n) {
		int i;
		double norm = 0.;

		for (i = start; i < n; ++i) {
			norm += Math.abs(x[i]);
		}

		return norm;
	}

	static void owlqnPseudoGradient(double[] pg, final double[] x, final double[] g, final int n, final double c,
			final int start, final int end) {
		int i;

		/* Compute the negative of gradients. */
		for (i = 0; i < start; ++i) {
			pg[i] = g[i];
		}

		/* Compute the psuedo-gradients. */
		for (i = start; i < end; ++i) {
			if (x[i] < 0.) {
				/* Differentiable. */
				pg[i] = g[i] - c;
			} else if (0. < x[i]) {
				/* Differentiable. */
				pg[i] = g[i] + c;
			} else {
				if (g[i] < -c) {
					/* Take the right partial derivative. */
					pg[i] = g[i] + c;
				} else if (c < g[i]) {
					/* Take the left partial derivative. */
					pg[i] = g[i] - c;
				} else {
					pg[i] = 0.;
				}
			}
		}

		for (i = end; i < n; ++i) {
			pg[i] = g[i];
		}
	}

	static void owlqnProject(double[] d, final double[] sign, final int start, final int end) {
		int i;

		for (i = start; i < end; ++i) {
			if (d[i] * sign[i] <= 0) {
				d[i] = 0;
			}
		}
	}

}
