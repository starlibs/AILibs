package ai.libs.jaicore.ml.regression.loss.dataset;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ai.libs.jaicore.ml.regression.loss.ERulPerformanceMeasure;

public class AsymmetricLossTest {

	@Test
	public void lossWithNoError() {
		List<Double> expected = new ArrayList<>();
		expected.add(0.1);
		expected.add(0.2);
		expected.add(0.3);
		expected.add(0.4);
		expected.add(0.5);
		List<Double> actual = new ArrayList<>();
		actual.add(0.1);
		actual.add(0.2);
		actual.add(0.3);
		actual.add(0.4);
		actual.add(0.5);
		ERulPerformanceMeasure asymmetricLoss = ERulPerformanceMeasure.ASYMMETRIC_LOSS;
		Double loss = asymmetricLoss.loss(actual, expected);
		assertEquals(Double.valueOf(1.0), loss);
	}

	@Test
	public void lossWithError() {
		List<Double> expected = new ArrayList<>();
		expected.add(10d);
		expected.add(5d);
		List<Double> actual = new ArrayList<>();
		actual.add(11d);
		actual.add(4d);
		ERulPerformanceMeasure asymmetricLoss = ERulPerformanceMeasure.ASYMMETRIC_LOSS;
		Double loss = asymmetricLoss.loss(expected, actual);
		assertEquals(Double.valueOf(0.375), loss);
	}

}
