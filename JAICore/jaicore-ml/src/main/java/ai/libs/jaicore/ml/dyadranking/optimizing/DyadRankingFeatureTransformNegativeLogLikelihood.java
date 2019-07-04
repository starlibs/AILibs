package ai.libs.jaicore.ml.dyadranking.optimizing;

import java.util.Map;

import ai.libs.jaicore.basic.algorithm.IOptimizationAlgorithm;
import ai.libs.jaicore.math.linearalgebra.Vector;
import ai.libs.jaicore.ml.dyadranking.Dyad;
import ai.libs.jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Implements the negative log-likelihood function for the feature
 * transformation Placket-Luce dyad ranker.
 *
 * In particular, this implmentation is the NLL of [1] (we adhere their notation
 * here). This NLL is a convex function, which we can optimize using an
 * {@link IOptimizationAlgorithm}, together with the
 * {@link DyadRankingFeatureTransformNegativeLogLikelihoodDerivative}.
 *
 *
 * [1] Schäfer, D. & Hüllermeier, Dyad ranking using Plackett–Luce models based
 * on joint feature representations,
 * https://link.springer.com/article/10.1007%2Fs10994-017-5694-9
 *
 * @author Helena Graf, Mirko Jürgens
 *
 */
public class DyadRankingFeatureTransformNegativeLogLikelihood
implements IDyadRankingFeatureTransformPLGradientDescendableFunction {

	/* the dataset used by this function */
	private DyadRankingDataset dataset;

	private Map<IDyadRankingInstance, Map<Dyad, Vector>> featureTransforms;

	@Override
	public void initialize(final DyadRankingDataset dataset, final Map<IDyadRankingInstance, Map<Dyad, Vector>> featureTransforms) {
		this.dataset = dataset;
		this.featureTransforms = featureTransforms;

	}

	/**
	 * Algorithm (18) of [1]. We adhere their notations, but, unify the sums.
	 */
	@Override
	public double apply(final Vector w) {
		double firstSum = 0d;
		double secondSum = 0d;
		int largeN = this.dataset.size();
		for (int smallN = 0; smallN < largeN; smallN++) {
			IDyadRankingInstance instance = this.dataset.get(smallN);
			int mN = instance.length();
			for (int m = 0; m < mN; m++) {
				Dyad dyad = instance.getDyadAtPosition(m);
				firstSum = firstSum + w.dotProduct(this.featureTransforms.get(instance).get(dyad));
				double innerSum = 0d;
				for (int l = m; l < mN - 1; l++) {
					Dyad innerDyad = instance.getDyadAtPosition(l);
					innerSum = innerSum + Math.exp(w.dotProduct(this.featureTransforms.get(instance).get(innerDyad)));
				}
				secondSum = secondSum + Math.log(innerSum);
			}
		}
		return -firstSum + secondSum;
	}

}
