package jaicore.ml.dyadranking.zeroshot.util;

import org.nd4j.linalg.api.ndarray.INDArray;

import jaicore.ml.dyadranking.util.DyadNormalScaler;
import weka.core.Utils;

public class ZeroShotUtil {
	
	public static String[] mapJ48InputsToWekaOptions(double C, double M) throws Exception {
		
		long roundedM = Math.round(M);
		
		String[] options = Utils.splitOptions("-C " + C + " -M " + roundedM);
		
		return options;		
	}
	
	public static String[] mapSMORBFInputsToWekaOptions(double CExp, double LExp, double RBFGammaExp) throws Exception {
		double C = Math.pow(10, CExp);
		double L = Math.pow(10, LExp);
		double G = Math.pow(10, RBFGammaExp);
		
		String C_complexity_const_option = "-C " + C;
		String L_tolerance_option = " -L " + L;
		String RBF_gamma_option =" -K \"weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G " + G + "\"";
		
		String options = 
				C_complexity_const_option +
				L_tolerance_option +
				RBF_gamma_option;
		
		String[] optionsSplit = Utils.splitOptions(options);
		
		return optionsSplit;
	}
	
	public static String[] mapMLPInputsToWekaOptions(double LExp, double MExp, double N) throws Exception {
		double L = Math.pow(10, LExp);
		double M = Math.pow(10, MExp);
		long roundedN = Math.round(N);
		
		String[] options = Utils.splitOptions("-L " + L + " -M " + M + " -N " + roundedN);
		
		return options;
	}
	
	public static String[] mapRFInputsToWekaOptions(double I, double KFraction, double M, double depth, double KNumAttributes) throws Exception {
		int I_rounded = (int) Math.round(I);
		int K = (int) Math.ceil(KNumAttributes * KFraction);
		int M_rounded = (int) Math.round(M);
		int depth_rounded = (int) Math.round(depth);
		
		String[] options = Utils.splitOptions(" -I " + I_rounded + " -K " + K + " -M " + M_rounded + " -depth " + depth_rounded);
		
		return options;
	}
	
	public static INDArray unscaleParameters(INDArray parameters, DyadNormalScaler scaler, int numHyperPars) {
		int[] hyperParIndices = new int[numHyperPars];
		for (int i = 0; i < numHyperPars; i++) {
			hyperParIndices[i] = (int) parameters.length() - numHyperPars + i;
		}
		INDArray unscaled = parameters.getColumns(hyperParIndices);
		for (int i = 0; i < unscaled.length(); i++) {
			unscaled.putScalar(i, 
					unscaled.getDouble(i) 
					* (scaler.getStatsY()[i].getMax() - scaler.getStatsY()[i].getMin())
					+  scaler.getStatsY()[i].getMin());
		}
		
		return unscaled;		
	}
}
