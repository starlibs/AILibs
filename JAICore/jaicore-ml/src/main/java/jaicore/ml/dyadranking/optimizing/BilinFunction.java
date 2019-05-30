package jaicore.ml.dyadranking.optimizing;

import java.util.Map;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import edu.stanford.nlp.optimization.DiffFunction;
import edu.stanford.nlp.optimization.QNMinimizer;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.featuretransform.BiliniearFeatureTransform;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Wraps the NLL optimizing problem into the {@link QNMinimizer} optimizer.
 * 
 * @author mirkoj
 *
 */
public class BilinFunction implements DiffFunction {
	/* The function to optimize */
	private DyadRankingFeatureTransformNegativeLogLikelihood function;
	/* The gradient */
	private DyadRankingFeatureTransformNegativeLogLikelihoodDerivative gradient;
	/* The dimension of the vector that should be optimized. */
	private int dimension;

	/**
	 * Creates a NLL optimizing problem for the kronecker product as the bilinear feature transform.
	 * @param featureTransform the feature transform, must be an instance of {@link BiliniearFeatureTransform}
	 * @param drDataset the dataset to optimize
	 * @param dimension the dimension of the optimized vector
	 */
	public BilinFunction(Map<IDyadRankingInstance, Map<Dyad, Vector>> featureTransforms, DyadRankingDataset drDataset, int dimension) {
		function = new DyadRankingFeatureTransformNegativeLogLikelihood();
		function.initialize(drDataset, featureTransforms);
		gradient = new DyadRankingFeatureTransformNegativeLogLikelihoodDerivative();
		gradient.initialize(drDataset, featureTransforms);
		this.dimension = dimension;
	}

	@Override
	public double valueAt(double[] x) {
		return function.apply(new DenseDoubleVector(x));
	}

	@Override
	public int domainDimension() {
		return dimension;
	}

	@Override
	public double[] derivativeAt(double[] x) {
		return gradient.apply(new DenseDoubleVector(x)).asArray();
	}

}
