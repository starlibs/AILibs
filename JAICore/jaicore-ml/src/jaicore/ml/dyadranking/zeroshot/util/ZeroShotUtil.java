package jaicore.ml.dyadranking.zeroshot.util;

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
		
		String[] options = Utils.splitOptions("-C " + C + " -L " + L +
				" - K \"weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G " + G + "\"");
		
		return options;
	}
	
	public static String[] mapMLPInputsToWekaOptions(double L, double M, double N) throws Exception {	
		String[] options = Utils.splitOptions("-L " + L + " -M " + M + " -N " + N);
		
		return options;
	}
	
	public static String[] mapRFInputsToWekaOptions(double P, double I, double KFraction, double KNumAttributes, double M, double VExp, double depth, double N) throws Exception {
		
		double V = Math.exp(VExp);
		int K = (int) Math.ceil(KNumAttributes * KFraction);
		
		String[] options = Utils.splitOptions("- P " + P + " -I " + I + " -K " + K + " -M " + M + " -V " + V + " -depth " + depth + " -N " + N);
		
		return options;
	}
}
