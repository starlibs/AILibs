package jaicore.modifiedISAC;

import java.util.List;

import jaicore.CustomDataTypes.ProblemInstance;
import weka.core.Instance;

public class Normalizer {
	private int numbervaluesToNormalize;
	private double[] maxvalues;
	private List<ProblemInstance<Instance>> basisForNormalization;

	/**
	 * @param list
	 */
	public Normalizer(List<ProblemInstance<Instance>> list) {
		this.numbervaluesToNormalize = list.get(0).getInstance().numAttributes();
		this.maxvalues = new double[numbervaluesToNormalize];
		this.basisForNormalization = list;
	}

	/**
	 * 
	 */
	public void setupnormalize() {
		for (ProblemInstance<Instance> i : basisForNormalization) {
			double[] instacnevector = i.getInstance().toDoubleArray();
			for (int j = 0; j < instacnevector.length; j++) {
				if (Double.isNaN(instacnevector[j])) {
					if (Double.isNaN(maxvalues[j])) {
						maxvalues[j] = Double.NaN;
					}
				} else {
					if (Double.isNaN(maxvalues[j])) {
						maxvalues[j] = Math.abs(instacnevector[j]);
					} else {
						if (Math.abs(instacnevector[j]) > maxvalues[j]) {
							maxvalues[j] = Math.abs(instacnevector[j]);
						}
					}
				}
			}
		}
	}
	//TODO Auch darauf achten das eine neue Instance größer sein kann als das bisher gefundene Maximum. Gilt nur für neu die anderen sind
	//Basis für das Setup ist deren globales Maximum.

	/**
	 * @param vectorToNormalize
	 * @return
	 */
	public double[] normalize(double[] vectorToNormalize) {
		for (int i = 0; i < vectorToNormalize.length; i++) {
			if (Double.isNaN(maxvalues[i])) {
				if (Double.isNaN(vectorToNormalize[i])) {
					vectorToNormalize[i] = Double.NaN;
				}
				else {
					if(vectorToNormalize[i]<0) {
						vectorToNormalize[i]=-1;
					}
					else {
						vectorToNormalize[i]=1;
					}
				}
			} else {
				if(Double.isNaN(vectorToNormalize[i])) {
					vectorToNormalize[i] = Double.NaN;
				}
				if(Math.abs(vectorToNormalize[i])>maxvalues[i]) {
					if(vectorToNormalize[i]>=0) {
						vectorToNormalize[i]=1;
					}
					else {
						vectorToNormalize[i]=-1;
					}
				}
				if(vectorToNormalize[i]<0) {
					vectorToNormalize[i] = (((Math.abs(vectorToNormalize[i]) / maxvalues[i]) * 2) - 1)*(-1);
				}
				else {
					vectorToNormalize[i] = ((Math.abs(vectorToNormalize[i]) / maxvalues[i]) * 2) - 1;
				}
			}
		}

		return vectorToNormalize;
	}
//TODO entfernen dieser Methode 
	public double[] getbasis() {
		return maxvalues;
	}
}
