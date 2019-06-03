package ai.libs.jaicore.ml.dyadranking.algorithm.featuretransform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.math.linearalgebra.Vector;
import ai.libs.jaicore.ml.core.exception.ConfigurationException;
import ai.libs.jaicore.ml.core.exception.PredictionException;
import ai.libs.jaicore.ml.core.exception.TrainingException;
import ai.libs.jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;
import ai.libs.jaicore.ml.dyadranking.Dyad;
import ai.libs.jaicore.ml.dyadranking.algorithm.IPLDyadRanker;
import ai.libs.jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import ai.libs.jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import ai.libs.jaicore.ml.dyadranking.optimizing.BilinFunction;
import ai.libs.jaicore.ml.dyadranking.optimizing.DyadRankingFeatureTransformNegativeLogLikelihood;
import ai.libs.jaicore.ml.dyadranking.optimizing.DyadRankingFeatureTransformNegativeLogLikelihoodDerivative;
import ai.libs.jaicore.ml.dyadranking.optimizing.IDyadRankingFeatureTransformPLGradientDescendableFunction;
import ai.libs.jaicore.ml.dyadranking.optimizing.IDyadRankingFeatureTransformPLGradientFunction;
import edu.stanford.nlp.optimization.QNMinimizer;

/**
 * A feature transformation Placket-Luce dyad ranker. By default uses bilinear
 * feature transformation.
 *
 * All the provided algorithms are implementations of the PLModel introduced in
 * [1].
 *
 *
 * [1] Schäfer, D. & Hüllermeier, Dyad ranking using Plackett-Luce models based
 * on joint feature representations,
 * https://link.springer.com/article/10.1007%2Fs10994-017-5694-9
 *
 * @author Helena Graf, Mirko JÃ¼rgens
 *
 */
public class FeatureTransformPLDyadRanker implements IPLDyadRanker {

	private static final Logger log = LoggerFactory.getLogger(FeatureTransformPLDyadRanker.class);

	/* Phi in the paper */
	private IDyadFeatureTransform featureTransform;

	/*
	 * The label-specific weight vector, that determines the linear function used to
	 * calculate the skill parameters
	 */
	private Vector w;

	/* The differentiable function optimized by the optimzer to find vector w */
	private IDyadRankingFeatureTransformPLGradientDescendableFunction negativeLogLikelihood = new DyadRankingFeatureTransformNegativeLogLikelihood();

	/* The derivation of the above function */
	private IDyadRankingFeatureTransformPLGradientFunction negativeLogLikelihoodDerivative = new DyadRankingFeatureTransformNegativeLogLikelihoodDerivative();

	/**
	 * Constructs a new feature transform Placket-Luce dyad ranker with bilinear
	 * feature transformation.
	 */
	public FeatureTransformPLDyadRanker() {
		this(new BiliniearFeatureTransform());
	}

	/**
	 * Constructs a new feature transform Placket-Luce dyad ranker with the given
	 * feature transformation method.
	 *
	 * @param featureTransform
	 *            the feature transformation method to use
	 */
	public FeatureTransformPLDyadRanker(final IDyadFeatureTransform featureTransform) {
		this.featureTransform = featureTransform;
	}

	@Override
	public IDyadRankingInstance predict(final IDyadRankingInstance instance) throws PredictionException {
		if (this.w == null) {
			throw new PredictionException("The Ranker has not been trained yet.");
		}
		log.debug("Training ranker with instance {}", instance);
		List<Pair<Double, Dyad>> skillForDyads = new ArrayList<>();

		for (Dyad d : instance) {
			double skill = this.computeSkillForDyad(d);
			skillForDyads.add(new Pair<Double, Dyad>(skill, d));
		}
		return new DyadRankingInstance(skillForDyads.stream().sorted((p1, p2) -> Double.compare(p1.getX(), p2.getX())).map(Pair::getY).collect(Collectors.toList()));
	}


	@Override
	public List<IDyadRankingInstance> predict(final DyadRankingDataset dataset) throws PredictionException {
		List<IDyadRankingInstance> predictions = new ArrayList<>();
		for (IDyadRankingInstance i : dataset) {
			predictions.add(this.predict(i));
		}
		return predictions;
	}

	private double computeSkillForDyad(final Dyad dyad) {
		Vector featureTransformVector = this.featureTransform.transform(dyad);
		double dot = this.w.dotProduct(featureTransformVector);
		double val = Math.exp(dot);
		log.debug("Feature transform for dyad {} is {}. \n Dot-Product is {} and skill is {}", dyad, featureTransformVector, dot, val);
		return val;
	}

	@Override
	public void train(final DyadRankingDataset dataset) throws TrainingException {
		Map<IDyadRankingInstance, Map<Dyad, Vector>> featureTransforms = this.featureTransform.getPreComputedFeatureTransforms(dataset);
		this.negativeLogLikelihood.initialize(dataset, featureTransforms);
		this.negativeLogLikelihoodDerivative.initialize(dataset, featureTransforms);

		int alternativeLength = dataset.get(0).getDyadAtPosition(0).getAlternative().length();
		int instanceLength = dataset.get(0).getDyadAtPosition(0).getInstance().length();
		this.w = new DenseDoubleVector(this.featureTransform.getTransformedVectorLength(alternativeLength, instanceLength), 0.3);
		log.debug("Likelihood of the randomly filled w is {}", this.likelihoodOfParameter(this.w, dataset));
		BilinFunction fun = new BilinFunction(featureTransforms, dataset, this.featureTransform.getTransformedVectorLength(alternativeLength, instanceLength));
		QNMinimizer minimizer = new QNMinimizer();
		this.w = new DenseDoubleVector(minimizer.minimize(fun, 0.01, this.w.asArray()));
		log.debug("Finished optimizing, the final w is {}", this.w);

	}

	/**
	 * Computes the likelihood of the parameter vector w. Algorithm (16) of [1].
	 *
	 * @param w
	 *            the likelihood to be computed
	 * @param dataset
	 *            the dataset on which the likelihood should be evaluated
	 * @return the likelihood, measured as a probability
	 */
	private double likelihoodOfParameter(final Vector w, final DyadRankingDataset dataset) {
		int largeN = dataset.size();
		double outerProduct = 1.0;
		for (int smallN = 0; smallN < largeN; smallN++) {
			IDyadRankingInstance dyadRankingInstance = dataset.get(smallN);
			int mN = dyadRankingInstance.length();
			double innerProduct = 1.0;
			for (int m = 0; m < mN; m++) {
				Dyad dyad = dyadRankingInstance.getDyadAtPosition(m);
				Vector zNM = this.featureTransform.transform(dyad);
				double en = Math.exp(w.dotProduct(zNM));
				double denumSum = 0;
				for (int l = m; l < mN; l++) {
					Dyad dyadL = dyadRankingInstance.getDyadAtPosition(l);
					Vector zNL = this.featureTransform.transform(dyadL);
					denumSum += Math.exp(w.dotProduct(zNL));
				}
				innerProduct = innerProduct * (en / denumSum);
			}

			outerProduct = outerProduct * innerProduct;
		}
		return outerProduct;
	}

	@Override
	public IPredictiveModelConfiguration getConfiguration() {
		/* Currently nothing to configure! */
		return null;
	}

	@Override
	public void setConfiguration(final IPredictiveModelConfiguration configuration) throws ConfigurationException {
		/* Currently nothing to configure */
	}
}
