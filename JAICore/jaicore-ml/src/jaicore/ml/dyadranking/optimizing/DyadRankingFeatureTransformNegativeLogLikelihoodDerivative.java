package jaicore.ml.dyadranking.optimizing;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.optimizing.graddesc.BlackBoxGradient;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.featuretransform.IDyadFeatureTransform;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Represents the derivate of the negative log likelihood function in the
 * context of feature transformation Placket-Luce dyad ranking [1].
 * 
 * This implementation can be used for the partial derivatives of the linear
 * vector <code>w</code> w.r.t. the negative log-likelihood that should be
 * minimized.
 * 
 * [1] Schäfer, D. & Hüllermeier, Dyad ranking using Plackett–Luce models based
 * on joint feature representations,
 * https://link.springer.com/article/10.1007%2Fs10994-017-5694-9Ï
 * 
 * @author Helena Graf, Mirko Jürgens
 *
 */
public class DyadRankingFeatureTransformNegativeLogLikelihoodDerivative
		implements IDyadRankingFeatureTransformPLGradientFunction {

	/* the dataset used by this function */
	private DyadRankingDataset dataset;

	/* the feature transformation method used by this function */
	private IDyadFeatureTransform featureTransform;

	@Override
	public void initialize(DyadRankingDataset dataset, IDyadFeatureTransform featureTransform) {
		this.dataset = dataset;
		this.featureTransform = featureTransform;
	}

	@Override
	public Vector apply(Vector vector) {
		Vector result = new DenseDoubleVector(vector.length());
		for (int i = 0; i < vector.length(); i++) {
			result.setValue(i, computeDerivativeForIndex(i, vector));
		}
		if (!result.stream().allMatch(Double::isFinite)) {
			DyadRankingFeatureTransformNegativeLogLikelihood function = new DyadRankingFeatureTransformNegativeLogLikelihood();
			function.initialize(dataset, featureTransform);
			//backup plan: estimate the gradient
			BlackBoxGradient bbg = new BlackBoxGradient(function, 0.1);
			Vector gradient = bbg.apply(vector);
			for (int i = 0; i < vector.length(); i++) {
				if (!Double.isFinite(result.getValue(i))) {
					result.setValue(i, gradient.getValue(i));
				}
			}
		}
		return result;
	}

	/**
	 * Computes the partial derivatives of every single w_i. Algorithm (19) of [1].
	 * 
	 * @param i
	 *            the index of the partial derivative.
	 * @param vector
	 *            the w vector
	 * @return the partial derivative w_i
	 */
	private double computeDerivativeForIndex(int i, Vector vector) {
		double result = 0;
		int N = dataset.size();
		for (int n = 0; n < N; n++) {
			IDyadRankingInstance instance = dataset.get(n);
			int M_n = instance.length();
			for (int m = 0; m < M_n - 1; m++) {
				double gValue = 1 / g(vector, instance, m);
				result += h(vector, instance, i, m) * gValue;
				result -= featureTransform.transform(instance.getDyadAtPosition(m)).getValue(i);

			}
		}

		return result;
	}

	private double h(Vector vector, IDyadRankingInstance instance, int i, int beginIndex) {
		double result = 0;
		int M_n = instance.length();
		for (int l = beginIndex; l < M_n; l++) {
			Dyad dyad = instance.getDyadAtPosition(l);
			Vector zNL = featureTransform.transform(dyad);
			result += zNL.getValue(i) * Math.exp(vector.dotProduct(zNL));
		}

		return result;
	}

	private double g(Vector w, IDyadRankingInstance instance, int beginIndex) {
		double result = 0;
		int M_n = instance.length();
		for (int l = beginIndex; l < M_n; l++) {
			Dyad dyad = instance.getDyadAtPosition(l);
			result += Math.exp(w.dotProduct(featureTransform.transform(dyad)));
		}
		return result;
	}

}
