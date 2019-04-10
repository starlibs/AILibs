package jaicore.ml.learningcurve.extrapolation;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;

import jaicore.ml.interfaces.LearningCurve;
import jaicore.ml.learningcurve.extrapolation.lc.LinearCombinationExtrapolationMethod;
import jaicore.ml.learningcurve.extrapolation.lc.LinearCombinationLearningCurve;
import jaicore.ml.learningcurve.extrapolation.lc.LinearCombinationLearningCurveConfiguration;
import jaicore.ml.learningcurve.extrapolation.lc.LinearCombinationParameterSet;

public class LinearCombinationLearningCurveTester {

	@Test
	public void testSaturationPointPow3() {
		LearningCurve lc = generateTestLearningCurvePow3();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(652.077, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testSaturationPointVapor() {
		LearningCurve lc = generateTestLearningCurveVapor();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(1210.469, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testSaturationPointLogLogLinear() {
		LearningCurve lc = generateTestLearningCurveLogLogLinear();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(1382.754, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testSaturationPointHill3() {
		LearningCurve lc = generateTestLearningCurveHill3();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(1214.202, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testSaturationPointLogPower() {
		LearningCurve lc = generateTestLearningCurveLogPower();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(719.621, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testSaturationPointPow4() {
		LearningCurve lc = generateTestLearningCurvePow4();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(555.803, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testSaturationPointMMF() {
		LearningCurve lc = generateTestLearningCurveMMF();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(631.508, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testSaturationPointExp4() {
		LearningCurve lc = generateTestLearningCurveExp4();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(906.084, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testSaturationPointJanoschek() {
		LearningCurve lc = generateTestLearningCurveJanoschek();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(526.875, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testSaturationPointWeibull() {
		LearningCurve lc = generateTestLearningCurveWeibull();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(395.219, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testSaturationPointIlog2() {
		LearningCurve lc = generateTestLearningCurveIlog2();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(305.460, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testSaturationPointCombination() {
		LearningCurve lc = generateTestLearningCurveCombination();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(907.201, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testSaturationPoint2ParameterSets() {
		LearningCurve lc = generateTestLearningCurve2ParameterSets();
		double saturationPoint = lc.getSaturationPoint(0.1);
		assertEquals(462.934, saturationPoint, 0.1, "The computed saturation point is not correct!");
	}

	@Test
	public void testConvergenceValuePow3() {
		LearningCurve lc = generateTestLearningCurvePow3();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(0.919, convergenceValue, 0.01, "The computed convergence value is not correct!");

	}

	@Test
	public void testConvergenceValueVapor() {
		LearningCurve lc = generateTestLearningCurveVapor();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(4.842, convergenceValue, 0.01, "The computed convergence value is not correct!");
	}

	@Test
	public void testConvergenceValueLogLogLinear() {
		LearningCurve lc = generateTestLearningCurveLogLogLinear();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(1.22, convergenceValue, 0.01, "The computed convergence value is not correct!");
	}

	@Test
	public void testConvergenceValueHill3() {
		LearningCurve lc = generateTestLearningCurveHill3();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(0.996, convergenceValue, 0.01, "The computed convergence value is not correct!");
	}

	@Test
	public void testConvergenceValueLogPower() {
		LearningCurve lc = generateTestLearningCurveLogPower();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(0.83, convergenceValue, 0.01, "The computed convergence value is not correct!");
	}

	@Test
	public void testConvergenceValuePow4() {
		LearningCurve lc = generateTestLearningCurvePow4();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(0.981, convergenceValue, 0.01, "The computed convergence value is not correct!");
	}

	@Test
	public void testConvergenceValueMMF() {
		LearningCurve lc = generateTestLearningCurveMMF();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(0.986, convergenceValue, 0.01, "The computed convergence value is not correct!");
	}

	@Test
	public void testConvergenceValueExp4() {
		LearningCurve lc = generateTestLearningCurveExp4();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(0.949, convergenceValue, 0.01, "The computed convergence value is not correct!");
	}

	@Test
	public void testConvergenceValueJanoschek() {
		LearningCurve lc = generateTestLearningCurveJanoschek();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(0.99, convergenceValue, 0.01, "The computed convergence value is not correct!");
	}

	@Test
	public void testConvergenceValueWeibull() {
		LearningCurve lc = generateTestLearningCurveWeibull();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(0.949, convergenceValue, 0.01, "The computed convergence value is not correct!");
	}

	@Test
	public void testConvergenceValueIlog2() {
		LearningCurve lc = generateTestLearningCurveIlog2();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(0.861, convergenceValue, 0.01, "The computed convergence value is not correct!");
	}

	@Test
	public void testConvergenceValueCombination() {
		LearningCurve lc = generateTestLearningCurveCombination();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(0.972, convergenceValue, 0.01, "The computed convergence value is not correct!");
	}

	@Test
	public void testConvergenceValue2ParameterSets() {
		LearningCurve lc = generateTestLearningCurve2ParameterSets();
		double convergenceValue = lc.getConvergenceValue();
		assertEquals(0.9562, convergenceValue, 0.01, "The computed convergence value is not correct!");
	}

	private LinearCombinationLearningCurve generateTestLearningCurvePow3() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();
		LinearCombinationParameterSet parameterSet = new LinearCombinationParameterSet();
		Map<String, Double> weights = new HashMap<>();
		weights.put("pow_3", 1.0);

		Map<String, Map<String, Double>> parameters = new HashMap<>();
		Map<String, Double> params = new HashMap<>();
		params.put("a", 1.8);
		params.put("c", 0.95);
		params.put("alpha", 0.35);

		parameters.put("pow_3", params);
		parameterSet.setWeights(weights);
		parameterSet.setParameters(parameters);
		configuration.setParameterSets(Collections.singletonList(parameterSet));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	private LinearCombinationLearningCurve generateTestLearningCurveVapor() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();
		LinearCombinationParameterSet parameterSet = new LinearCombinationParameterSet();
		Map<String, Double> weights = new HashMap<>();
		weights.put("vapor_pressure", 1.0);

		Map<String, Map<String, Double>> parameters = new HashMap<>();
		Map<String, Double> params = new HashMap<>();
		params.put("a", -2.5);
		params.put("b", -0.01);
		params.put("c", 0.25);

		parameters.put("vapor_pressure", params);
		parameterSet.setWeights(weights);
		parameterSet.setParameters(parameters);
		configuration.setParameterSets(Collections.singletonList(parameterSet));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	private LinearCombinationLearningCurve generateTestLearningCurveLogLogLinear() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();
		LinearCombinationParameterSet parameterSet = new LinearCombinationParameterSet();
		Map<String, Double> weights = new HashMap<>();
		weights.put("log_log_linear", 1.0);

		Map<String, Map<String, Double>> parameters = new HashMap<>();
		Map<String, Double> params = new HashMap<>();
		params.put("a", 0.25);
		params.put("b", 0.0);

		parameters.put("log_log_linear", params);
		parameterSet.setWeights(weights);
		parameterSet.setParameters(parameters);
		configuration.setParameterSets(Collections.singletonList(parameterSet));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	private LinearCombinationLearningCurve generateTestLearningCurveHill3() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();
		LinearCombinationParameterSet parameterSet = new LinearCombinationParameterSet();
		Map<String, Double> weights = new HashMap<>();
		weights.put("hill_3", 1.0);

		Map<String, Map<String, Double>> parameters = new HashMap<>();
		Map<String, Double> params = new HashMap<>();
		params.put("y", 1.0);
		params.put("eta", 1.0);
		params.put("kappa", 200.0);

		parameters.put("hill_3", params);
		parameterSet.setWeights(weights);
		parameterSet.setParameters(parameters);
		configuration.setParameterSets(Collections.singletonList(parameterSet));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	private LinearCombinationLearningCurve generateTestLearningCurveLogPower() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();
		LinearCombinationParameterSet parameterSet = new LinearCombinationParameterSet();
		Map<String, Double> weights = new HashMap<>();
		weights.put("log_power", 1.0);

		Map<String, Map<String, Double>> parameters = new HashMap<>();
		Map<String, Double> params = new HashMap<>();
		params.put("a", 0.85);
		params.put("b", 4.0);
		params.put("c", -0.5);

		parameters.put("log_power", params);
		parameterSet.setWeights(weights);
		parameterSet.setParameters(parameters);
		configuration.setParameterSets(Collections.singletonList(parameterSet));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	private LinearCombinationLearningCurve generateTestLearningCurvePow4() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();
		LinearCombinationParameterSet parameterSet = new LinearCombinationParameterSet();
		Map<String, Double> weights = new HashMap<>();
		weights.put("pow_4", 1.0);

		Map<String, Map<String, Double>> parameters = new HashMap<>();
		Map<String, Double> params = new HashMap<>();
		params.put("a", 0.25);
		params.put("b", 0.0);
		params.put("c", 1.0);
		params.put("alpha", 0.4);

		parameters.put("pow_4", params);
		parameterSet.setWeights(weights);
		parameterSet.setParameters(parameters);
		configuration.setParameterSets(Collections.singletonList(parameterSet));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	private LinearCombinationLearningCurve generateTestLearningCurveMMF() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();
		LinearCombinationParameterSet parameterSet = new LinearCombinationParameterSet();
		Map<String, Double> weights = new HashMap<>();
		weights.put("mmf", 1.0);

		Map<String, Map<String, Double>> parameters = new HashMap<>();
		Map<String, Double> params = new HashMap<>();
		params.put("alpha", 1.0);
		params.put("beta", -1.0);
		params.put("delta", 0.5);
		params.put("kappa", 0.3);

		parameters.put("mmf", params);
		parameterSet.setWeights(weights);
		parameterSet.setParameters(parameters);
		configuration.setParameterSets(Collections.singletonList(parameterSet));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	private LinearCombinationLearningCurve generateTestLearningCurveExp4() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();
		LinearCombinationParameterSet parameterSet = new LinearCombinationParameterSet();
		Map<String, Double> weights = new HashMap<>();
		weights.put("exp_4", 1.0);

		Map<String, Map<String, Double>> parameters = new HashMap<>();
		Map<String, Double> params = new HashMap<>();
		params.put("a", 0.1);
		params.put("b", 0.2);
		params.put("c", 0.95);
		params.put("alpha", 0.5);

		parameters.put("exp_4", params);
		parameterSet.setWeights(weights);
		parameterSet.setParameters(parameters);
		configuration.setParameterSets(Collections.singletonList(parameterSet));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	private LinearCombinationLearningCurve generateTestLearningCurveJanoschek() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();
		LinearCombinationParameterSet parameterSet = new LinearCombinationParameterSet();
		Map<String, Double> weights = new HashMap<>();
		weights.put("janoschek", 1.0);

		Map<String, Map<String, Double>> parameters = new HashMap<>();
		Map<String, Double> params = new HashMap<>();
		params.put("alpha", 1.0);
		params.put("beta", -0.5);
		params.put("delta", 0.6);
		params.put("kappa", 0.1);

		parameters.put("janoschek", params);
		parameterSet.setWeights(weights);
		parameterSet.setParameters(parameters);
		configuration.setParameterSets(Collections.singletonList(parameterSet));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	private LinearCombinationLearningCurve generateTestLearningCurveWeibull() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();
		LinearCombinationParameterSet parameterSet = new LinearCombinationParameterSet();
		Map<String, Double> weights = new HashMap<>();
		weights.put("weibull", 1.0);

		Map<String, Map<String, Double>> parameters = new HashMap<>();
		Map<String, Double> params = new HashMap<>();
		params.put("alpha", 0.95);
		params.put("beta", -2.0);
		params.put("delta", 0.2);
		params.put("kappa", 3.0);

		parameters.put("weibull", params);
		parameterSet.setWeights(weights);
		parameterSet.setParameters(parameters);
		configuration.setParameterSets(Collections.singletonList(parameterSet));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	private LinearCombinationLearningCurve generateTestLearningCurveIlog2() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();
		LinearCombinationParameterSet parameterSet = new LinearCombinationParameterSet();
		Map<String, Double> weights = new HashMap<>();
		weights.put("ilog_2", 1.0);

		Map<String, Map<String, Double>> parameters = new HashMap<>();
		Map<String, Double> params = new HashMap<>();
		params.put("a", 1.0);
		params.put("c", 0.95);

		parameters.put("ilog_2", params);
		parameterSet.setWeights(weights);
		parameterSet.setParameters(parameters);
		configuration.setParameterSets(Collections.singletonList(parameterSet));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	private LinearCombinationLearningCurve generateTestLearningCurveCombination() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();
		LinearCombinationParameterSet parameterSet = new LinearCombinationParameterSet();
		Map<String, Double> weights = new HashMap<>();
		weights.put("pow_3", 1.0 / 3.0);
		weights.put("log_log_linear", 1.0 / 3.0);
		weights.put("log_power", 1.0 / 3.0);

		Map<String, Map<String, Double>> parameters = new HashMap<>();
		Map<String, Double> pow3Params = new HashMap<>();
		pow3Params.put("a", 1.8);
		pow3Params.put("c", 0.95);
		pow3Params.put("alpha", 0.35);
		parameters.put("pow_3", pow3Params);

		Map<String, Double> logLogLinearParams = new HashMap<>();
		logLogLinearParams.put("a", 0.25);
		logLogLinearParams.put("b", 0.0);
		parameters.put("log_log_linear", logLogLinearParams);

		Map<String, Double> logPowerParams = new HashMap<>();
		logPowerParams.put("a", 0.85);
		logPowerParams.put("b", 4.0);
		logPowerParams.put("c", -0.5);
		parameters.put("log_power", logPowerParams);

		parameterSet.setWeights(weights);
		parameterSet.setParameters(parameters);
		configuration.setParameterSets(Collections.singletonList(parameterSet));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	private LinearCombinationLearningCurve generateTestLearningCurve2ParameterSets() {
		LinearCombinationLearningCurveConfiguration configuration = new LinearCombinationLearningCurveConfiguration();

		// Parameter set 1
		LinearCombinationParameterSet parameterSet1 = new LinearCombinationParameterSet();
		Map<String, Double> weights1 = new HashMap<>();
		weights1.put("pow_3", 1.0);

		Map<String, Map<String, Double>> parameters1 = new HashMap<>();
		Map<String, Double> pow3Params1 = new HashMap<>();
		pow3Params1.put("a", 1.8);
		pow3Params1.put("c", 0.95);
		pow3Params1.put("alpha", 0.35);
		parameters1.put("pow_3", pow3Params1);

		parameterSet1.setParameters(parameters1);
		parameterSet1.setWeights(weights1);

		// Parameter set 2
		LinearCombinationParameterSet parameterSet2 = new LinearCombinationParameterSet();
		Map<String, Double> weights2 = new HashMap<>();
		weights2.put("pow_3", 1.0);

		Map<String, Map<String, Double>> parameters2 = new HashMap<>();
		Map<String, Double> pow3Params2 = new HashMap<>();
		pow3Params2.put("a", 2.0);
		pow3Params2.put("c", 1.0);
		pow3Params2.put("alpha", 0.7);
		parameters2.put("pow_3", pow3Params2);

		parameterSet2.setWeights(weights2);
		parameterSet2.setParameters(parameters2);
		configuration.setParameterSets(Lists.newArrayList(parameterSet1, parameterSet2));
		return new LinearCombinationLearningCurve(configuration, 2000);
	}

	@Test
	public void completeTest() throws InvalidAnchorPointsException {
		// Generate learning curve from real anchor points
		int[] xValues = { 4, 8, 16, 32, 64, 128, 256 };
		double[] yValues = { 0.1672, 0.2513, 0.4161, 0.5485, 0.7381, 0.7961, 0.8471 };
		LinearCombinationExtrapolationMethod method = new LinearCombinationExtrapolationMethod();

		LearningCurve lc = method.extrapolateLearningCurveFromAnchorPoints(xValues, yValues, 2000);

		checkValuesBetween0And1(lc);

		checkMonotoneIncreasing(lc);

		assertTrue(lc.getSaturationPoint(0.1) > 0 && lc.getSaturationPoint(0.1) < 2000);

		assertTrue(lc.getConvergenceValue() > 0 && lc.getConvergenceValue() < 1);
	}

	private void checkValuesBetween0And1(LearningCurve lc) {
		// We tolerate some negative values near 0
		for (int x = 10; x < 2000; x++) {
			double y = lc.getCurveValue(x);
			assertTrue(String.format("Curve value is not between 0 and 1 for x=%d (y=%f)", x, y), y >= 0 && y <= 1);
		}
	}

	private void checkMonotoneIncreasing(LearningCurve lc) {
		// We tolerate unexpected behavior near 0
		for (int x = 11; x < 2000; x++) {
			assertTrue(String.format("Curve value is not monotonic increasing between x=%d and x=%d", x, x - 1),
					lc.getCurveValue(x) >= lc.getCurveValue(x - 1));
		}
	}

}
