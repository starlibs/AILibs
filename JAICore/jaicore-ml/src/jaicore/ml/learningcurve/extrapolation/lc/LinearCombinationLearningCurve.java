package jaicore.ml.learningcurve.extrapolation.lc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

import jaicore.ml.interfaces.LearningCurve;

/**
 * The LinearCombinationLearningCurve consists of the actual linear combination
 * function that describes the learning curve, as well as the derivative of this
 * function. The derivative is used in order to calculate the saturation point.
 * 
 * @author Felix Weiland
 *
 */
public class LinearCombinationLearningCurve implements LearningCurve {

	/** The (extrapolated) learning curve function */
	private LinearCombinationFunction learningCurve;

	/** The derivative of the learning curve */
	private LinearCombinationFunction derivative;

	public LinearCombinationLearningCurve(LinearCombinationConfiguration configuration) {
		this.generateLearningCurve(configuration);
		this.generateDerivative(configuration);
	}

	private void generateLearningCurve(LinearCombinationConfiguration configuration) {
		List<ParametricFunction> functions = new ArrayList<>();
		List<Double> weights = new ArrayList<>();

		// Vapor pressure
		ParametricFunction vaporPressure = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.VAPOR_PRESSURE)) {

			@Override
			public double getValue(double x) {
				double a = this.getParams().get(LinearCombinationConstants.A);
				double b = this.getParams().get(LinearCombinationConstants.B);
				double c = this.getParams().get(LinearCombinationConstants.C);
				return Math.exp(a + (b / x) + c * Math.log(x));
			}
		};
		functions.add(vaporPressure);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.VAPOR_PRESSURE));

		// pow_3
		ParametricFunction pow3 = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.POW_3)) {

			@Override
			public double getValue(double x) {
				double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
				double a = this.getParams().get(LinearCombinationConstants.A);
				double c = this.getParams().get(LinearCombinationConstants.C);
				return c - a * Math.pow(x, -1 * alpha);
			}
		};
		functions.add(pow3);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.POW_3));

		// log log linear
		ParametricFunction logLogLinear = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.LOG_LOG_LINEAR)) {

			@Override
			public double getValue(double x) {
				double a = this.getParams().get(LinearCombinationConstants.A);
				double b = this.getParams().get(LinearCombinationConstants.B);
				return Math.log(a * Math.log(x) + b);
			}
		};
		functions.add(logLogLinear);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.LOG_LOG_LINEAR));

		// hill3
		ParametricFunction hill3 = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.HILL_3)) {

			@Override
			public double getValue(double x) {
				double y = this.getParams().get(LinearCombinationConstants.Y);
				double eta = this.getParams().get(LinearCombinationConstants.ETA);
				double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
				return (y * Math.pow(x, eta)) / (Math.pow(kappa, eta) + Math.pow(x, eta));
			}
		};
		functions.add(hill3);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.HILL_3));

		// log power
		ParametricFunction logPower = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.LOG_POWER)) {

			@Override
			public double getValue(double x) {
				double a = this.getParams().get(LinearCombinationConstants.A);
				double b = this.getParams().get(LinearCombinationConstants.B);
				double c = this.getParams().get(LinearCombinationConstants.C);
				return a / (1 + Math.pow(x / Math.exp(b), c));
			}
		};
		functions.add(logPower);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.LOG_POWER));

		// pow4
		ParametricFunction pow4 = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.POW_4)) {

			@Override
			public double getValue(double x) {
				double a = this.getParams().get(LinearCombinationConstants.A);
				double b = this.getParams().get(LinearCombinationConstants.B);
				double c = this.getParams().get(LinearCombinationConstants.C);
				double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
				return c - Math.pow(a * x + b, -alpha);
			}
		};
		functions.add(pow4);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.POW_4));

		// MMF
		ParametricFunction mmf = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.MMF)) {

			@Override
			public double getValue(double x) {
				double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
				double beta = this.getParams().get(LinearCombinationConstants.BETA);
				double delta = this.getParams().get(LinearCombinationConstants.DELTA);
				double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
				return alpha - ((alpha - beta) / (1 + Math.pow(kappa * x, delta)));
			}
		};
		functions.add(mmf);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.MMF));

		// exp4
		ParametricFunction exp4 = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.EXP_4)) {

			@Override
			public double getValue(double x) {
				double a = this.getParams().get(LinearCombinationConstants.A);
				double b = this.getParams().get(LinearCombinationConstants.B);
				double c = this.getParams().get(LinearCombinationConstants.C);
				double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
				return c - Math.exp(-a * Math.pow(x, alpha) + b);
			}
		};
		functions.add(exp4);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.EXP_4));

		// Janoschek
		ParametricFunction janoscheck = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.JANOSCHECK)) {

			@Override
			public double getValue(double x) {
				double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
				double beta = this.getParams().get(LinearCombinationConstants.BETA);
				double delta = this.getParams().get(LinearCombinationConstants.DELTA);
				double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
				return alpha - (alpha - beta) * Math.exp(-kappa * Math.pow(x, delta));
			}
		};
		functions.add(janoscheck);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.JANOSCHECK));

		// Weibull
		ParametricFunction weibull = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.WEIBULL)) {

			@Override
			public double getValue(double x) {
				double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
				double beta = this.getParams().get(LinearCombinationConstants.BETA);
				double delta = this.getParams().get(LinearCombinationConstants.DELTA);
				double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
				return alpha - (alpha - beta) * Math.exp(-1 * Math.pow(kappa * x, delta));
			}
		};
		functions.add(weibull);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.WEIBULL));

		// ilog2
		ParametricFunction ilog2 = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.ILOG_2)) {

			@Override
			public double getValue(double x) {
				double a = this.getParams().get(LinearCombinationConstants.A);
				double c = this.getParams().get(LinearCombinationConstants.C);
				return c - (a / Math.log(x));
			}
		};
		functions.add(ilog2);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.ILOG_2));

		this.learningCurve = new LinearCombinationFunction(functions, weights, 0);
	}

	private void generateDerivative(LinearCombinationConfiguration configuration) {
		List<ParametricFunction> functions = new ArrayList<>();
		List<Double> weights = new ArrayList<>();

		// Vapor pressure
		ParametricFunction vaporPressure = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.VAPOR_PRESSURE)) {

			@Override
			public double getValue(double x) {
				double a = this.getParams().get(LinearCombinationConstants.A);
				double b = this.getParams().get(LinearCombinationConstants.B);
				double c = this.getParams().get(LinearCombinationConstants.C);
				return Math.pow(x, c - 2) * Math.exp(a + (b / x)) + (c * x - b);
			}
		};
		functions.add(vaporPressure);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.VAPOR_PRESSURE));

		// pow_3
		ParametricFunction pow3 = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.POW_3)) {

			@Override
			public double getValue(double x) {
				double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
				double a = this.getParams().get(LinearCombinationConstants.A);
				return a * alpha * Math.pow(x, -alpha - 1);
			}
		};
		functions.add(pow3);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.POW_3));

		// log log linear
		ParametricFunction logLogLinear = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.LOG_LOG_LINEAR)) {

			@Override
			public double getValue(double x) {
				double a = this.getParams().get(LinearCombinationConstants.A);
				double b = this.getParams().get(LinearCombinationConstants.B);
				return a / (a * x * Math.log(x) + b * x);
			}
		};
		functions.add(logLogLinear);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.LOG_LOG_LINEAR));

		// hill3
		ParametricFunction hill3 = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.HILL_3)) {

			@Override
			public double getValue(double x) {
				double y = this.getParams().get(LinearCombinationConstants.Y);
				double eta = this.getParams().get(LinearCombinationConstants.ETA);
				double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
				return (y * eta * Math.pow(kappa, eta) * Math.pow(x, eta - 1))
						/ (Math.pow(Math.pow(kappa, eta) + Math.pow(x, eta), 2));
			}
		};
		functions.add(hill3);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.HILL_3));

		// log power
		ParametricFunction logPower = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.LOG_POWER)) {

			@Override
			public double getValue(double x) {
				double a = this.getParams().get(LinearCombinationConstants.A);
				double b = this.getParams().get(LinearCombinationConstants.B);
				double c = this.getParams().get(LinearCombinationConstants.C);
				return (a * c * Math.pow(Math.exp(-b) + x, c)) / (x * Math.pow(Math.pow(Math.exp(-b) * x, c) + 1, 2));
			}
		};
		functions.add(logPower);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.LOG_POWER));

		// pow4
		ParametricFunction pow4 = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.POW_4)) {

			@Override
			public double getValue(double x) {
				double a = this.getParams().get(LinearCombinationConstants.A);
				double b = this.getParams().get(LinearCombinationConstants.B);
				double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
				return a * alpha * Math.pow(a * x + b, -alpha - 1);
			}
		};
		functions.add(pow4);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.POW_4));

		// MMF
		ParametricFunction mmf = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.MMF)) {

			@Override
			public double getValue(double x) {
				double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
				double beta = this.getParams().get(LinearCombinationConstants.BETA);
				double delta = this.getParams().get(LinearCombinationConstants.DELTA);
				double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
				return (delta * (alpha - beta) * Math.pow(kappa * x, delta))
						/ (x * Math.pow(1 + Math.pow(kappa * x, delta), 2));
			}
		};
		functions.add(mmf);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.MMF));

		// exp4
		ParametricFunction exp4 = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.EXP_4)) {

			@Override
			public double getValue(double x) {
				double a = this.getParams().get(LinearCombinationConstants.A);
				double b = this.getParams().get(LinearCombinationConstants.B);
				double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
				return a * alpha * Math.pow(x, alpha - 1) * Math.exp(b - a * Math.pow(x, alpha));
			}
		};
		functions.add(exp4);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.EXP_4));

		// Janoschek
		ParametricFunction janoscheck = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.JANOSCHECK)) {

			@Override
			public double getValue(double x) {
				double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
				double beta = this.getParams().get(LinearCombinationConstants.BETA);
				double delta = this.getParams().get(LinearCombinationConstants.DELTA);
				double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
				return kappa * delta * (alpha - beta) * Math.pow(x, delta - 1) * Math.exp(-kappa * Math.pow(x, delta));
			}
		};
		functions.add(janoscheck);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.JANOSCHECK));

		// Weibull
		ParametricFunction weibull = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.WEIBULL)) {

			@Override
			public double getValue(double x) {
				double alpha = this.getParams().get(LinearCombinationConstants.ALPHA);
				double beta = this.getParams().get(LinearCombinationConstants.BETA);
				double delta = this.getParams().get(LinearCombinationConstants.DELTA);
				double kappa = this.getParams().get(LinearCombinationConstants.KAPPA);
				return (delta * (alpha - beta) * Math.exp(-1 * Math.pow(kappa * x, delta)) * Math.pow(kappa * x, delta))
						/ x;
			}
		};
		functions.add(weibull);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.WEIBULL));

		// ilog2
		ParametricFunction ilog2 = new ParametricFunction(
				configuration.getParameters().get(LinearCombinationConstants.ILOG_2)) {

			@Override
			public double getValue(double x) {
				double a = this.getParams().get(LinearCombinationConstants.A);
				return a / (x * Math.pow(Math.log(x), 2));
			}
		};
		functions.add(ilog2);
		weights.add(configuration.getWeights().get(LinearCombinationConstants.ILOG_2));

		this.derivative = new LinearCombinationFunction(functions, weights, -0.2);
	}

	@Override
	public double getCurveValue(double x) {
		return this.learningCurve.value(x);
	}

	@Override
	public double getSaturationPoint(double epsilon) {
		UnivariateSolver solver = new BrentSolver(0, epsilon);
		// TODO: Optimize interval
		return solver.solve(100, this.derivative, 0, Integer.MAX_VALUE);
	}

	@Override
	public double getDerivativeCurveValue(double x) {
		return this.derivative.value(x);
	}
}
