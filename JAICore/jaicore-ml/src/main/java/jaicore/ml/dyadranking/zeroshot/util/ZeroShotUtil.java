package jaicore.ml.dyadranking.zeroshot.util;

import org.nd4j.linalg.api.ndarray.INDArray;

import jaicore.ml.dyadranking.util.DyadMinMaxScaler;
import weka.core.Utils;

/**
 * A collection of utility methods used to map the results of a input optimization of {@link PLNetInputOptimizer} back to Weka options for the respective classifiers.
 * @author Michael Braun
 *
 */
public class ZeroShotUtil {
	
	private ZeroShotUtil() {
		// Intentionally left blank
	}

	public static String[] mapJ48InputsToWekaOptions(double c, double m) throws Exception {
		long roundedM = Math.round(m);

		return Utils.splitOptions("-C " + c + " -M " + roundedM);
	}

	public static String[] mapSMORBFInputsToWekaOptions(double cExp, double rbfGammaExp) throws Exception {
		double c = Math.pow(10, cExp);
		double g = Math.pow(10, rbfGammaExp);

		String cComplexityConstOption = "-C " + c;
		String rbfGammaOption = " -K \"weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G " + g + "\"";

		String options = cComplexityConstOption + rbfGammaOption;

		return Utils.splitOptions(options);
	}

	public static String[] mapMLPInputsToWekaOptions(double lExp, double mExp, double n) throws Exception {
		double l = Math.pow(10, lExp);
		double m = Math.pow(10, mExp);
		long roundedN = Math.round(n);

		return Utils.splitOptions("-L " + l + " -M " + m + " -N " + roundedN);
	}

	public static String[] mapRFInputsToWekaOptions(double i, double kFraction, double m, double depth, double kNumAttributes) throws Exception {
		int iRounded = (int) Math.round(i);
		int k = (int) Math.ceil(kNumAttributes * kFraction);
		int mRounded = (int) Math.round(m);
		int depthRounded = (int) Math.round(depth);

		return Utils.splitOptions(" -I " + iRounded + " -K " + k + " -M " + mRounded + " -depth " + depthRounded);
	}

	public static INDArray unscaleParameters(INDArray parameters, DyadMinMaxScaler scaler, int numHyperPars) {
		int[] hyperParIndices = new int[numHyperPars];
		for (int i = 0; i < numHyperPars; i++) {
			hyperParIndices[i] = (int) parameters.length() - numHyperPars + i;
		}
		INDArray unscaled = parameters.getColumns(hyperParIndices);
		for (int i = 0; i < unscaled.length(); i++) {
			unscaled.putScalar(i, unscaled.getDouble(i) * (scaler.getStatsY()[i].getMax() - scaler.getStatsY()[i].getMin()) + scaler.getStatsY()[i].getMin());
		}

		return unscaled;
	}
}
