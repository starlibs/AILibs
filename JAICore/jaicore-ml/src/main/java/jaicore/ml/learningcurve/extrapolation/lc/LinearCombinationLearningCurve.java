package jaicore.ml.learningcurve.extrapolation.lc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.interfaces.AnalyticalLearningCurve;

/**
 * The LinearCombinationLearningCurve consists of the actual linear combination
 * function that describes the learning curve, as well as the derivative of this
 * function. The derivative is used in order to calculate the saturation point.
 * 
 * @author Felix Weiland
 *
 */
public class LinearCombinationLearningCurve implements AnalyticalLearningCurve {

	private static final Logger LOG = LoggerFactory.getLogger(LinearCombinationLearningCurve.class);

	/**
	 * Constant value describing the number of times the size of the interval in
	 * which a root is searched is doubled
	 */
	private static final int ROOT_COMPUTATION_RETIRES = 8;

	/**
	 * Constant value describing the slope at which we assume to have reached the
	 * saturation point
	 */
	private static final double SLOPE_SATURATION_POINT = 0.0001;

	/**
	 * Error tolerance for root computation in case of the convergence value
	 * calculation.
	 */
	private static final double TOLERANCE_CONVERGENCE_VALUE = 1.0;

	/**
	 * Constant value describing the slope at which we assume that there is no
	 * significant change in the curve value anymore and the convergence value is
	 * reached.
	 */
	private static final double SLOPE_CONVERGENCE_VALUE = 0.0000001;

	/** The (extrapolated) learning curve function */
	private LinearCombinationFunction learningCurve;

	/** The derivative of the learning curve */
	private LinearCombinationFunction derivative;

	/** Size of the data set this learning curve was produced on */
	private int dataSetSize;

	public LinearCombinationLearningCurve(LinearCombinationLearningCurveConfiguration configuration, int dataSetSize) {
		List<UnivariateFunction> learningCurves = new ArrayList<>();
		List<UnivariateFunction> derivatives = new ArrayList<>();

		for (LinearCombinationParameterSet parameterSet : configuration.getParameterSets()) {
			learningCurves.add(generateLearningCurve(parameterSet));
			derivatives.add(generateDerivative(parameterSet));
		}

		List<Double> weights = new ArrayList<>();
		for (int i = 0; i < configuration.getParameterSets().size(); i++) {
			weights.add(1.0 / configuration.getParameterSets().size());
		}

		this.learningCurve = new LinearCombinationFunction(learningCurves, weights);
		this.derivative = new LinearCombinationFunction(derivatives, weights);

		this.dataSetSize = dataSetSize;
	}

	private LinearCombinationFunction generateLearningCurve(LinearCombinationParameterSet parameterSet) {
		List<UnivariateFunction> functions = new ArrayList<>();
		List<Double> weights = new ArrayList<>();

		// Vapor pressure
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.VAPOR_PRESSURE)) {
			ParametricFunction vaporPressure = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.VAPOR_PRESSURE)) {

				@Override
				public double value(double x) {
					double a = this.getParams().get(LinearCombinationConstants.A);
					double b = this.getParams().get(LinearCombinationConstants.B);
					double c = this.getParams().get(LinearCombinationConstants.C);
					return Math.exp(a + (b / x) + c * Math.log(x));
				}
			};
			functions.add(vaporPressure);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.VAPOR_PRESSURE));
		}

		// pow_3
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.POW_3)) {
			ParametricFunction pow3 = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.POW_3)) {

				@Override
				public double value(double x) {
					double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
					double a = this.getParams().get(LinearCombinationConstants.A);
					double c = this.getParams().get(LinearCombinationConstants.C);
					return c - a * Math.pow(x, -1 * alpha);
				}
			};
			functions.add(pow3);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.POW_3));
		}

		// log log linear
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.LOG_LOG_LINEAR)) {
			ParametricFunction logLogLinear = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.LOG_LOG_LINEAR)) {

				@Override
				public double value(double x) {
					double a = this.getParams().get(LinearCombinationConstants.A);
					double b = this.getParams().get(LinearCombinationConstants.B);
					return Math.log(a * Math.log(x) + b);
				}
			};
			functions.add(logLogLinear);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.LOG_LOG_LINEAR));
		}

		// hill3
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.HILL_3)) {
			ParametricFunction hill3 = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.HILL_3)) {

				@Override
				public double value(double x) {
					double y = this.getParams().get(LinearCombinationConstants.Y);
					double eta = this.getParams().get(LinearCombinationConstants.ETA);
					double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
					return (y * Math.pow(x, eta)) / (Math.pow(kappa, eta) + Math.pow(x, eta));
				}
			};
			functions.add(hill3);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.HILL_3));
		}

		// log power
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.LOG_POWER)) {
			ParametricFunction logPower = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.LOG_POWER)) {

				@Override
				public double value(double x) {
					double a = this.getParams().get(LinearCombinationConstants.A);
					double b = this.getParams().get(LinearCombinationConstants.B);
					double c = this.getParams().get(LinearCombinationConstants.C);
					return a / (1 + Math.pow(x / Math.exp(b), c));
				}
			};
			functions.add(logPower);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.LOG_POWER));
		}

		// pow4
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.POW_4)) {
			ParametricFunction pow4 = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.POW_4)) {

				@Override
				public double value(double x) {
					double a = this.getParams().get(LinearCombinationConstants.A);
					double b = this.getParams().get(LinearCombinationConstants.B);
					double c = this.getParams().get(LinearCombinationConstants.C);
					double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
					return c - Math.pow(a * x + b, -alpha);
				}
			};
			functions.add(pow4);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.POW_4));
		}

		// MMF
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.MMF)) {
			ParametricFunction mmf = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.MMF)) {

				@Override
				public double value(double x) {
					double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
					double beta = this.getParams().get(LinearCombinationConstants.BETA);
					double delta = this.getParams().get(LinearCombinationConstants.DELTA);
					double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
					return alpha - ((alpha - beta) / (1 + Math.pow(kappa * x, delta)));
				}
			};
			functions.add(mmf);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.MMF));
		}

		// exp4
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.EXP_4)) {
			ParametricFunction exp4 = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.EXP_4)) {

				@Override
				public double value(double x) {
					double a = this.getParams().get(LinearCombinationConstants.A);
					double b = this.getParams().get(LinearCombinationConstants.B);
					double c = this.getParams().get(LinearCombinationConstants.C);
					double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
					return c - Math.exp(-a * Math.pow(x, alpha) + b);
				}
			};
			functions.add(exp4);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.EXP_4));
		}

		// Janoschek
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.JANOSCHEK)) {
			ParametricFunction janoscheck = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.JANOSCHEK)) {

				@Override
				public double value(double x) {
					double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
					double beta = this.getParams().get(LinearCombinationConstants.BETA);
					double delta = this.getParams().get(LinearCombinationConstants.DELTA);
					double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
					return alpha - (alpha - beta) * Math.exp(-kappa * Math.pow(x, delta));
				}
			};
			functions.add(janoscheck);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.JANOSCHEK));
		}

		// Weibull
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.WEIBULL)) {
			ParametricFunction weibull = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.WEIBULL)) {

				@Override
				public double value(double x) {
					double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
					double beta = this.getParams().get(LinearCombinationConstants.BETA);
					double delta = this.getParams().get(LinearCombinationConstants.DELTA);
					double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
					return alpha - (alpha - beta) * Math.exp(-1 * Math.pow(kappa * x, delta));
				}
			};
			functions.add(weibull);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.WEIBULL));
		}

		// ilog2
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.ILOG_2)) {
			ParametricFunction ilog2 = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.ILOG_2)) {

				@Override
				public double value(double x) {
					double a = this.getParams().get(LinearCombinationConstants.A);
					double c = this.getParams().get(LinearCombinationConstants.C);
					return c - (a / Math.log(x));
				}
			};
			functions.add(ilog2);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.ILOG_2));
		}

		return new LinearCombinationFunction(functions, weights);
	}

	private LinearCombinationFunction generateDerivative(LinearCombinationParameterSet parameterSet) {
		List<UnivariateFunction> functions = new ArrayList<>();
		List<Double> weights = new ArrayList<>();

		// Vapor pressure
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.VAPOR_PRESSURE)) {
			ParametricFunction vaporPressure = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.VAPOR_PRESSURE)) {

				@Override
				public double value(double x) {
					double a = this.getParams().get(LinearCombinationConstants.A);
					double b = this.getParams().get(LinearCombinationConstants.B);
					double c = this.getParams().get(LinearCombinationConstants.C);
					return Math.pow(x, c - 2) * Math.exp(a + b / x) * (c * x - b);
				}
			};
			functions.add(vaporPressure);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.VAPOR_PRESSURE));
		}

		// pow_3
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.POW_3)) {
			ParametricFunction pow3 = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.POW_3)) {

				@Override
				public double value(double x) {
					double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
					double a = this.getParams().get(LinearCombinationConstants.A);
					return a * alpha * Math.pow(x, -alpha - 1);
				}
			};
			functions.add(pow3);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.POW_3));
		}

		// log log linear
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.LOG_LOG_LINEAR)) {
			ParametricFunction logLogLinear = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.LOG_LOG_LINEAR)) {

				@Override
				public double value(double x) {
					double a = this.getParams().get(LinearCombinationConstants.A);
					double b = this.getParams().get(LinearCombinationConstants.B);
					return a / (a * x * Math.log(x) + b * x);
				}
			};
			functions.add(logLogLinear);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.LOG_LOG_LINEAR));
		}

		// hill3
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.HILL_3)) {
			ParametricFunction hill3 = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.HILL_3)) {

				@Override
				public double value(double x) {
					double y = this.getParams().get(LinearCombinationConstants.Y);
					double eta = this.getParams().get(LinearCombinationConstants.ETA);
					double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
					return (y * eta * Math.pow(kappa, eta) * Math.pow(x, eta - 1))
							/ (Math.pow(Math.pow(kappa, eta) + Math.pow(x, eta), 2));
				}
			};
			functions.add(hill3);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.HILL_3));
		}

		// log power
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.LOG_POWER)) {
			ParametricFunction logPower = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.LOG_POWER)) {

				@Override
				public double value(double x) {
					double a = this.getParams().get(LinearCombinationConstants.A);
					double b = this.getParams().get(LinearCombinationConstants.B);
					double c = this.getParams().get(LinearCombinationConstants.C);
					return -1 * (a * c * Math.pow(Math.exp(-b) * x, c))
							/ (x * Math.pow(Math.pow(Math.exp(-b) * x, c) + 1, 2));
				}
			};
			functions.add(logPower);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.LOG_POWER));
		}

		// pow4
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.POW_4)) {
			ParametricFunction pow4 = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.POW_4)) {

				@Override
				public double value(double x) {
					double a = this.getParams().get(LinearCombinationConstants.A);
					double b = this.getParams().get(LinearCombinationConstants.B);
					double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
					return a * alpha * Math.pow(a * x + b, -alpha - 1);
				}
			};
			functions.add(pow4);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.POW_4));
		}

		// MMF
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.MMF)) {
			ParametricFunction mmf = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.MMF)) {

				@Override
				public double value(double x) {
					double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
					double beta = this.getParams().get(LinearCombinationConstants.BETA);
					double delta = this.getParams().get(LinearCombinationConstants.DELTA);
					double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
					return (delta * (alpha - beta) * Math.pow(kappa * x, delta))
							/ (x * Math.pow(1 + Math.pow(kappa * x, delta), 2));
				}
			};
			functions.add(mmf);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.MMF));
		}

		// exp4
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.EXP_4)) {
			ParametricFunction exp4 = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.EXP_4)) {

				@Override
				public double value(double x) {
					double a = this.getParams().get(LinearCombinationConstants.A);
					double b = this.getParams().get(LinearCombinationConstants.B);
					double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
					return a * alpha * Math.pow(x, alpha - 1) * Math.exp(b - a * Math.pow(x, alpha));
				}
			};
			functions.add(exp4);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.EXP_4));
		}

		// Janoschek
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.JANOSCHEK)) {
			ParametricFunction janoscheck = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.JANOSCHEK)) {

				@Override
				public double value(double x) {
					double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
					double beta = this.getParams().get(LinearCombinationConstants.BETA);
					double delta = this.getParams().get(LinearCombinationConstants.DELTA);
					double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
					return kappa * delta * (alpha - beta) * Math.pow(x, delta - 1)
							* Math.exp(-kappa * Math.pow(x, delta));
				}
			};
			functions.add(janoscheck);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.JANOSCHEK));
		}

		// Weibull
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.WEIBULL)) {
			ParametricFunction weibull = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.WEIBULL)) {

				@Override
				public double value(double x) {
					double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
					double beta = this.getParams().get(LinearCombinationConstants.BETA);
					double delta = this.getParams().get(LinearCombinationConstants.DELTA);
					double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
					return (delta * (alpha - beta) * Math.exp(-1 * Math.pow(kappa * x, delta))
							* Math.pow(kappa * x, delta)) / x;
				}
			};
			functions.add(weibull);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.WEIBULL));
		}

		// ilog2
		if (parameterSet.getParameters().containsKey(LinearCombinationConstants.ILOG_2)) {
			ParametricFunction ilog2 = new ParametricFunction(
					parameterSet.getParameters().get(LinearCombinationConstants.ILOG_2)) {

				@Override
				public double value(double x) {
					double a = this.getParams().get(LinearCombinationConstants.A);
					return a / (x * Math.pow(Math.log(x), 2));
				}
			};
			functions.add(ilog2);
			weights.add(parameterSet.getWeights().get(LinearCombinationConstants.ILOG_2));
		}

		return new LinearCombinationFunction(functions, weights);
	}

	@Override
	public double getCurveValue(double x) {
		return learningCurve.value(x);
	}

	@Override
	public double getSaturationPoint(double epsilon) {
		return this.computeDerivativeRoot(epsilon, -1 * SLOPE_SATURATION_POINT, dataSetSize);
	}

	@Override
	public double getDerivativeCurveValue(double x) {
		this.derivative.setOffset(0);
		return this.derivative.value(x);
	}

	@Override
	public double getConvergenceValue() {
		int x = (int) this.computeDerivativeRoot(TOLERANCE_CONVERGENCE_VALUE, -1 * SLOPE_CONVERGENCE_VALUE,
				dataSetSize * 100);
		return this.getCurveValue(x);
	}

	private double computeDerivativeRoot(double epsilon, double offset, int upperIntervalBoundStart) {
		UnivariateSolver solver = new BrentSolver(0, epsilon);

		double result = -1;
		int lowerIntervalBound = 1;
		int upperIntervalBound = upperIntervalBoundStart;
		int retriesLeft = ROOT_COMPUTATION_RETIRES;

		this.derivative.setOffset(offset);

		while (retriesLeft > 0 && result == -1) {
			try {
				LOG.info("Trying to find root with offset {} in interval [{}/{}]", offset, lowerIntervalBound,
						upperIntervalBound);
				result = solver.solve(1000, this.derivative, lowerIntervalBound, upperIntervalBound);
			} catch (NoBracketingException e) {
				LOG.warn("Cannot find root in interval [{},{}]: {}", lowerIntervalBound, upperIntervalBound,
						e.getMessage());
				retriesLeft--;
				LOG.warn("Retries left: {} / {}", retriesLeft, ROOT_COMPUTATION_RETIRES);
				upperIntervalBound *= 2;
				lowerIntervalBound *= 2;
			}
		}

		// Try higher lower bound (sometimes functions behave unexpected close to 0)
		if (result == -1) {
			try {
				LOG.info("Trying to find root with offset {} in interval [{}/{}]", offset, lowerIntervalBound,
						upperIntervalBound);

				result = solver.solve(1000, this.derivative, 50, upperIntervalBound);
			} catch (NoBracketingException e) {
				LOG.warn("Cannot find root in interval [{},{}]: {}", lowerIntervalBound, upperIntervalBound,
						e.getMessage());
			}
		}

		if (result == -1) {
			throw new RuntimeException(
					String.format("No solution could be found in interval [1,%d]", upperIntervalBound));
		}
		return result;
	}
}
