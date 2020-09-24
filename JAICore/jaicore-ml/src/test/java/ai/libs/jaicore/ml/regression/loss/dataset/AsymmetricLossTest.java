package ai.libs.jaicore.ml.regression.loss.dataset;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.regression.loss.ERulPerformanceMeasure;

public class AsymmetricLossTest extends ARegressionLossTest {

	@Test
	public void lossWithNoError() {
		List<Double> expected = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
		List<IRegressionPrediction> predicted = this.toPredictions(expected);

		ERulPerformanceMeasure asymmetricLoss = ERulPerformanceMeasure.ASYMMETRIC_LOSS;
		assertEquals("Loss must be 0.0", 0.0, asymmetricLoss.loss(expected, predicted), 1E-8);
	}

	@Test
	public void lossWithError() {
		List<Double> expected = Arrays.asList(10.0, 5.0);
		List<IRegressionPrediction> predicted = this.toPredictions(Arrays.asList(11.0, 4.0));

		ERulPerformanceMeasure asymmetricLoss = ERulPerformanceMeasure.ASYMMETRIC_LOSS;
		assertEquals("Loss not as expected.", 0.625, asymmetricLoss.loss(expected, predicted), 1E-8);
	}

}
