package jaicore.ml.ranking.clusterbased.modifiedisac;

import java.util.List;

import jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import weka.core.Instance;

public class Normalizer {
	private int numbervaluesToNormalize;
	private double[] maxvalues;
	private List<ProblemInstance<Instance>> basisForNormalization;

	/**
	 * @param list
	 */
	public Normalizer(final List<ProblemInstance<Instance>> list) {
		this.numbervaluesToNormalize = list.get(0).getInstance().numAttributes();
		this.maxvalues = new double[this.numbervaluesToNormalize];
		this.basisForNormalization = list;
	}

	/**
	 *
	 */
	public void setupnormalize() {
		for (ProblemInstance<Instance> i : this.basisForNormalization) {
			double[] instacnevector = i.getInstance().toDoubleArray();
			for (int j = 0; j < instacnevector.length; j++) {
				if (Double.isNaN(instacnevector[j])) {
					if (Double.isNaN(this.maxvalues[j])) {
						this.maxvalues[j] = Double.NaN;
					}
				} else {
					if (Double.isNaN(this.maxvalues[j])) {
						this.maxvalues[j] = Math.abs(instacnevector[j]);
					} else {
						if (Math.abs(instacnevector[j]) > this.maxvalues[j]) {
							this.maxvalues[j] = Math.abs(instacnevector[j]);
						}
					}
				}
			}
		}
	}

	/**
	 * @param vectorToNormalize
	 * @return
	 */
	public double[] normalize(final double[] vectorToNormalize) {
		for (int i = 0; i < vectorToNormalize.length; i++) {
			if (Double.isNaN(this.maxvalues[i])) {
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
				if(Math.abs(vectorToNormalize[i])>this.maxvalues[i]) {
					if(vectorToNormalize[i]>=0) {
						vectorToNormalize[i]=1;
					}
					else {
						vectorToNormalize[i]=-1;
					}
				}
				if(vectorToNormalize[i]<0) {
					vectorToNormalize[i] = (((Math.abs(vectorToNormalize[i]) / this.maxvalues[i]) * 2) - 1)*(-1);
				}
				else {
					vectorToNormalize[i] = ((Math.abs(vectorToNormalize[i]) / this.maxvalues[i]) * 2) - 1;
				}
			}
		}

		return vectorToNormalize;
	}
}
