package jaicore.ml.learningcurve.extrapolation;

import static org.junit.Assert.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.learningcurve.extrapolation.lcnet.LCNetExtrapolationMethod;
import jaicore.ml.learningcurve.extrapolation.lcnet.PointWiseLearningCurve;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LCNetLearningCurveTest {

	@Test
	public void bTestNet() throws InvalidAnchorPointsException {
		String identifier = "abc";
		int dataSetSize = 10;
		int xValue = 5;
		double[] configurations = { 3, 0 };
		LCNetExtrapolationMethod lcNetExtrapolationMethod = new LCNetExtrapolationMethod(identifier);
		lcNetExtrapolationMethod.setConfigurations(configurations);
		PointWiseLearningCurve learningCurve = (PointWiseLearningCurve) lcNetExtrapolationMethod
				.extrapolateLearningCurveFromAnchorPoints(null, null, dataSetSize);
		double yValue = learningCurve.getCurveValue(xValue);
		assertTrue(yValue > 0);
		assertTrue(yValue < 2);
	}

	@Test
	public void aTrainNet() throws TrainingException {
		String identifier = "abc";
		int dataSetSize = 10;
		int[] xValues = new int[20];
		double[] yValues = new double[20];
		double[][] configurations = new double[20][2];
		for (int i = 0; i < xValues.length; i++) {
			xValues[i] = i % 10 + 1;
			yValues[i] = Math.sqrt((i % 10 + 1) / 10);
			configurations[i][0] = i % 4;
			configurations[i][1] = i % 2;
		}
		// 409 is here ok, only 500 should not occur
		LCNetExtrapolationMethod lcNetExtrapolationMethod = new LCNetExtrapolationMethod(identifier);
		lcNetExtrapolationMethod.trainNet(xValues, yValues, dataSetSize, configurations);
		assertTrue(true);
	}

}
