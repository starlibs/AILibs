package ai.libs.jaicore.ml.dyadranking.optimizing;

import java.util.Map;

import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.math.linearalgebra.Vector;
import ai.libs.jaicore.ml.dyadranking.Dyad;
import ai.libs.jaicore.ml.dyadranking.algorithm.featuretransform.BiliniearFeatureTransform;
import ai.libs.jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import edu.stanford.nlp.optimization.DiffFunction;
import edu.stanford.nlp.optimization.QNMinimizer;

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
	public BilinFunction(final Map<IDyadRankingInstance, Map<Dyad, Vector>> featureTransforms, final DyadRankingDataset drDataset, final int dimension) {
		this.function = new DyadRankingFeatureTransformNegativeLogLikelihood();
		this.function.initialize(drDataset, featureTransforms);
		this.gradient = new DyadRankingFeatureTransformNegativeLogLikelihoodDerivative();
		this.gradient.initialize(drDataset, featureTransforms);
		this.dimension = dimension;
	}

	@Override
	public double valueAt(final double[] x) {
		return this.function.apply(new DenseDoubleVector(x));
	}

	@Override
	public int domainDimension() {
		return this.dimension;
	}

	@Override
	public double[] derivativeAt(final double[] x) {
		return this.gradient.apply(new DenseDoubleVector(x)).asArray();
	}

}
