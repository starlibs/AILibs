package jaicore.ml.dyadranking.optimizing;

import java.util.HashMap;

import de.upb.isys.linearalgebra.Vector;
import jaicore.basic.algorithm.IOptimizationAlgorithm;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.featuretransform.IDyadFeatureTransform;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

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

	/* the feature transformation method used by this function */
	private IDyadFeatureTransform featureTransform;

	@Override
	public void initialize(DyadRankingDataset dataset, IDyadFeatureTransform featureTransform) {
		this.dataset = dataset;
		this.featureTransform = featureTransform;
	}

/**
 * Algorithm (18) of [1]. We adhere their notations, but, unify the sums.
 */
	@Override
	public double apply(Vector w) {
		HashMap<Dyad, Vector> featureTransforms = new HashMap<>();
		double result = 0;
		int N = dataset.size();
		for (int n = 0; n < N; n++) {
			IDyadRankingInstance instance = dataset.get(n);
			int M_n = instance.length();
			for (int m = 0; m < M_n; m++) {
				Dyad dyad = instance.getDyadAtPosition(m);
				result -= w.dotProduct(getOrCreateFeatureTransform(dyad, featureTransforms));
				double innerSum =0;
				
				for (int l = m; l < M_n; l++) {
					Dyad innerDyad = instance.getDyadAtPosition(l);
					
					innerSum+= Math
							.exp(w.dotProduct(getOrCreateFeatureTransform(innerDyad, featureTransforms)));
				}
				result += Math.log(innerSum);
			}
		}

		return result;
	}

	private Vector getOrCreateFeatureTransform(Dyad dyad, HashMap<Dyad, Vector> featureTransforms) {
		if (featureTransforms.containsKey(dyad)) {
			return featureTransforms.get(dyad);
		} else {
			Vector newFeatureTransform = featureTransform.transform(dyad);
			featureTransforms.put(dyad, newFeatureTransform);
			return newFeatureTransform;
		}
	}

}
