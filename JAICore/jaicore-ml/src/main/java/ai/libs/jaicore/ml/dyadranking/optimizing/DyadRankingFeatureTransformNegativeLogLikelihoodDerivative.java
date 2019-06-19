package ai.libs.jaicore.ml.dyadranking.optimizing;

import java.util.Map;

import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.math.linearalgebra.Vector;
import ai.libs.jaicore.ml.dyadranking.Dyad;
import ai.libs.jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

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
 * https://link.springer.com/article/10.1007%2Fs10994-017-5694-9
 *
 * @author Helena Graf, Mirko Jürgens
 *
 */
public class DyadRankingFeatureTransformNegativeLogLikelihoodDerivative
implements IDyadRankingFeatureTransformPLGradientFunction {

	/* the dataset used by this function */
	private DyadRankingDataset dataset;

	private Map<IDyadRankingInstance, Map<Dyad, Vector>> featureTransforms;

	@Override
	public void initialize(final DyadRankingDataset dataset, final Map<IDyadRankingInstance, Map<Dyad, Vector>> featureTransforms) {
		this.dataset = dataset;
		this.featureTransforms = featureTransforms;
	}

	@Override
	public Vector apply(final Vector vector) {
		Vector result = new DenseDoubleVector(vector.length());
		for (int i = 0; i < vector.length(); i++) {
			result.setValue(i, this.computeDerivativeForIndex(i, vector));
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
	private double computeDerivativeForIndex(final int i, final Vector vector) {
		double secondSum = 0d;
		int largeN = this.dataset.size();
		double firstSum = 0d;
		for (int smallN = 0; smallN < largeN; smallN++) {
			IDyadRankingInstance instance = this.dataset.get(smallN);
			int mN = instance.length();
			for (int m = 0; m < mN - 1; m++) {
				double innerDenumerator = 0d;
				double innerNumerator = 0d;
				Dyad dyad = instance.getDyadAtPosition(m);
				firstSum = firstSum + this.featureTransforms.get(instance).get(dyad).getValue(i);
				for (int l = m; l < mN; l++) {
					Vector zNL = this.featureTransforms.get(instance).get(instance.getDyadAtPosition(l));
					double dotProd = Math.exp(vector.dotProduct(zNL));
					innerNumerator = innerNumerator + zNL.getValue(i) * dotProd;
					innerDenumerator = innerDenumerator + dotProd;
				}

				if (innerDenumerator != 0) {
					secondSum = secondSum + innerNumerator / innerDenumerator;
				}
			}
		}
		return -firstSum + secondSum;
	}

}
