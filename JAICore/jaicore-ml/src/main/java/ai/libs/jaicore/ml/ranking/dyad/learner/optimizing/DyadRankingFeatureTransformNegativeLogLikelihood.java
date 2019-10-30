package ai.libs.jaicore.ml.ranking.dyad.learner.optimizing;

import java.util.Map;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingDataset;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.algorithm.IOptimizationAlgorithm;
import org.api4.java.common.math.IVector;

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
public class DyadRankingFeatureTransformNegativeLogLikelihood implements IDyadRankingFeatureTransformPLGradientDescendableFunction {

	/* the dataset used by this function */
	private IDyadRankingDataset dataset;

	private Map<IDyadRankingInstance, Map<IDyad, IVector>> featureTransforms;

	@Override
	public void initialize(final IDyadRankingDataset dataset, final Map<IDyadRankingInstance, Map<IDyad, IVector>> featureTransforms) {
		this.dataset = dataset;
		this.featureTransforms = featureTransforms;

	}

	/**
	 * Algorithm (18) of [1]. We adhere their notations, but, unify the sums.
	 */
	@Override
	public double apply(final IVector w) {
		double firstSum = 0d;
		double secondSum = 0d;
		int largeN = this.dataset.size();
		for (int smallN = 0; smallN < largeN; smallN++) {
			IDyadRankingInstance instance = this.dataset.get(smallN);
			int mN = instance.getNumberOfRankedElements();
			for (int m = 0; m < mN; m++) {
				IDyad dyad = instance.getLabel().get(m);
				firstSum = firstSum + w.dotProduct(this.featureTransforms.get(instance).get(dyad));
				double innerSum = 0d;
				for (int l = m; l < mN - 1; l++) {
					IDyad innerDyad = instance.getLabel().get(l);
					innerSum = innerSum + Math.exp(w.dotProduct(this.featureTransforms.get(instance).get(innerDyad)));
				}
				secondSum = secondSum + Math.log(innerSum);
			}
		}
		return -firstSum + secondSum;
	}

}
