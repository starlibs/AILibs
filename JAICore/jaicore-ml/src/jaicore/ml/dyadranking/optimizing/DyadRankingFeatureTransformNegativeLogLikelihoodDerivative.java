package jaicore.ml.dyadranking.optimizing;

import java.util.HashMap;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.IDyadFeatureTransform;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;

/**
 * Represents the derivate of the negative log likelihood function in the
 * context of feature transformation Placket-Luce dyad ranking.
 * 
 * @author Helena Graf
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

		HashMap<Pair<Vector, DyadRankingInstance>, Double> preComputedGValues = new HashMap<>();

		Vector result = new DenseDoubleVector(vector.length());

		for (int i = 0; i < vector.length(); i++) {
			result.setValue(i, computeDerivativeForIndex(i, vector, preComputedGValues));
		}

		return result;
	}

	private double computeDerivativeForIndex(int i, Vector vector,
			HashMap<Pair<Vector, DyadRankingInstance>, Double> preComputedGValues) {
		double result = 0;

		for (IInstance instance : dataset) {

			for (int j = 0; j < ((DyadRankingInstance) instance).length() - 1; j++) {
				double gValue = getOrCreateG(vector, ((DyadRankingInstance) instance), preComputedGValues);
				if (gValue == 0) {
					throw new ArithmeticException("Cannot divide by 0!");
				} else {
					result += h(vector, ((DyadRankingInstance) instance), i) / gValue;
					result -= featureTransform.transform(((DyadRankingInstance) instance).getDyadAtPosition(j))
							.getValue(i);
				}
			}
		}

		return result;
	}

	private double h(Vector vector, DyadRankingInstance instance, int i) {
		double result = 0;

		for (Dyad dyad : instance) {
			Vector zNL = featureTransform.transform(dyad);

			result += zNL.getValue(i) * Math.exp(vector.dotProduct(zNL));
		}

		return result;
	}

	private double getOrCreateG(Vector vector, DyadRankingInstance instance,
			HashMap<Pair<Vector, DyadRankingInstance>, Double> preComputedGValues) {
		Pair<Vector, DyadRankingInstance> newPair = new Pair<>(vector, instance);
		if (preComputedGValues.containsKey(newPair)) {
			return preComputedGValues.get(newPair);
		} else {
			double gValue = g(vector, instance);
			preComputedGValues.put(newPair, gValue);
			return gValue;
		}
	}

	private double g(Vector vector, DyadRankingInstance instance) {
		double result = 0;

		for (Dyad dyad : instance) {
			result += Math.exp(vector.dotProduct(featureTransform.transform(dyad)));
		}

		return result;
	}

}
