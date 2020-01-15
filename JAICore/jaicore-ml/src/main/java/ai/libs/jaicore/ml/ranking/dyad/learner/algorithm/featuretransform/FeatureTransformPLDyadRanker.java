package ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.featuretransform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.IRankingPredictionBatch;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingDataset;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.ranking.RankingPredictionBatch;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.IPLDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.optimizing.BilinFunction;
import ai.libs.jaicore.ml.ranking.dyad.learner.optimizing.DyadRankingFeatureTransformNegativeLogLikelihood;
import ai.libs.jaicore.ml.ranking.dyad.learner.optimizing.DyadRankingFeatureTransformNegativeLogLikelihoodDerivative;
import ai.libs.jaicore.ml.ranking.dyad.learner.optimizing.IDyadRankingFeatureTransformPLGradientDescendableFunction;
import ai.libs.jaicore.ml.ranking.dyad.learner.optimizing.IDyadRankingFeatureTransformPLGradientFunction;
import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.Ranking;
import edu.stanford.nlp.optimization.QNMinimizer;

/**
 * A feature transformation Plackett-Luce dyad ranker. By default uses bilinear
 * feature transformation.
 *
 * All the provided algorithms are implementations of the PLModel introduced in
 * [1].
 *
 *
 * [1] Sch�fer, D. & H�llermeier, Dyad ranking using Plackett-Luce models based
 * on joint feature representations,
 * https://link.springer.com/article/10.1007%2Fs10994-017-5694-9
 *
 * @author Helena Graf, Mirko Jürgens
 *
 */
public class FeatureTransformPLDyadRanker extends ASupervisedLearner<IDyadRankingInstance, IDyadRankingDataset, IRanking<IDyad>, IRankingPredictionBatch> implements IPLDyadRanker {

	private static final Logger log = LoggerFactory.getLogger(FeatureTransformPLDyadRanker.class);

	/* Phi in the paper */
	private IDyadFeatureTransform featureTransform;

	/*
	 * The label-specific weight vector, that determines the linear function used to
	 * calculate the skill parameters
	 */
	private IVector w;

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

	private double computeSkillForDyad(final IDyad dyad) {
		IVector featureTransformVector = this.featureTransform.transform(dyad);
		double dot = this.w.dotProduct(featureTransformVector);
		double val = Math.exp(dot);
		log.debug("Feature transform for dyad {} is {}. \n Dot-Product is {} and skill is {}", dyad, featureTransformVector, dot, val);
		return val;
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
	private double likelihoodOfParameter(final IVector w, final IDyadRankingDataset dataset) {
		int largeN = dataset.size();
		double outerProduct = 1.0;
		for (int smallN = 0; smallN < largeN; smallN++) {
			IDyadRankingInstance dyadRankingInstance = dataset.get(smallN);
			int mN = dyadRankingInstance.getNumberOfRankedElements();
			double innerProduct = 1.0;
			for (int m = 0; m < mN; m++) {
				IDyad dyad = dyadRankingInstance.getLabel().get(m);
				IVector zNM = this.featureTransform.transform(dyad);
				double en = Math.exp(w.dotProduct(zNM));
				double denumSum = 0;
				for (int l = m; l < mN; l++) {
					IDyad dyadL = dyadRankingInstance.getLabel().get(l);
					IVector zNL = this.featureTransform.transform(dyadL);
					denumSum += Math.exp(w.dotProduct(zNL));
				}
				innerProduct = innerProduct * (en / denumSum);
			}

			outerProduct = outerProduct * innerProduct;
		}
		return outerProduct;
	}

	@Override
	public void fit(final IDyadRankingDataset dataset) throws TrainingException, InterruptedException {
		Map<IDyadRankingInstance, Map<IDyad, IVector>> featureTransforms = this.featureTransform.getPreComputedFeatureTransforms(dataset);
		this.negativeLogLikelihood.initialize(dataset, featureTransforms);
		this.negativeLogLikelihoodDerivative.initialize(dataset, featureTransforms);

		int alternativeLength = dataset.get(0).getLabel().get(0).getAlternative().length();
		int instanceLength = dataset.get(0).getLabel().get(0).getContext().length();
		this.w = new DenseDoubleVector(this.featureTransform.getTransformedVectorLength(alternativeLength, instanceLength), 0.3);
		log.debug("Likelihood of the randomly filled w is {}", this.likelihoodOfParameter(this.w, dataset));
		BilinFunction fun = new BilinFunction(featureTransforms, dataset, this.featureTransform.getTransformedVectorLength(alternativeLength, instanceLength));
		QNMinimizer minimizer = new QNMinimizer();
		this.w = new DenseDoubleVector(minimizer.minimize(fun, 0.01, this.w.asArray()));
		log.debug("Finished optimizing, the final w is {}", this.w);
	}

	@Override
	public IRanking<IDyad> predict(final IDyadRankingInstance instance) throws PredictionException, InterruptedException {
		if (this.w == null) {
			throw new PredictionException("The Ranker has not been trained yet.");
		}
		log.debug("Training ranker with instance {}", instance);
		List<Pair<Double, IDyad>> skillForDyads = new ArrayList<>();

		for (IDyad d : instance) {
			double skill = this.computeSkillForDyad(d);
			skillForDyads.add(new Pair<>(skill, d));
		}
		return new Ranking<>(skillForDyads.stream().sorted((p1, p2) -> Double.compare(p1.getX(), p2.getX())).map(Pair<Double, IDyad>::getY).collect(Collectors.toList()));
	}

	@Override
	public IRankingPredictionBatch predict(IDyadRankingInstance[] dTest) throws PredictionException, InterruptedException {
		List<IRanking<?>> rankings = new ArrayList<>();
		for (IDyadRankingInstance instance : dTest) {
			rankings.add(predict(instance));
		}
		return new RankingPredictionBatch(rankings);
	}

}
