package jaicore.ml.dyadranking.algorithm.featuretransform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import edu.stanford.nlp.optimization.QNMinimizer;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.ConfigurationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.APLDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.optimizing.BilinFunction;
import jaicore.ml.dyadranking.optimizing.DyadRankingFeatureTransformNegativeLogLikelihood;
import jaicore.ml.dyadranking.optimizing.DyadRankingFeatureTransformNegativeLogLikelihoodDerivative;
import jaicore.ml.dyadranking.optimizing.IDyadRankingFeatureTransformPLGradientDescendableFunction;
import jaicore.ml.dyadranking.optimizing.IDyadRankingFeatureTransformPLGradientFunction;

/**
 * A feature transformation Placket-Luce dyad ranker. By default uses bilinear
 * feature transformation.
 * 
 * All the provided algorithms are implementations of the PLModel introduced in
 * [1].
 * 
 * 
 * [1] Schäfer, D. & Hüllermeier, Dyad ranking using Plackett–Luce models based
 * on joint feature representations,
 * https://link.springer.com/article/10.1007%2Fs10994-017-5694-9
 * 
 * @author Helena Graf, Mirko Jürgens
 *
 */
public class FeatureTransformPLDyadRanker extends APLDyadRanker {

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
	public FeatureTransformPLDyadRanker(IDyadFeatureTransform featureTransform) {
		this.featureTransform = featureTransform;
	}

	@Override
	public IDyadRankingInstance predict(IInstance instance) throws PredictionException {
		if (!(instance instanceof IDyadRankingInstance)) {
			throw new IllegalArgumentException(
					"FeatureTransformDyadRanker can only be used with IDyadRankingInstances.");
		}
		if (w == null) {
			throw new PredictionException("The Ranker has not been trained yet.");
		}
		log.debug("Training ranker with instance {}", instance);
		IDyadRankingInstance dyadRankingInstance = (IDyadRankingInstance) instance;
		List<Pair<Double, Dyad>> skillForDyads = new ArrayList<>();
		
		for (Dyad d : dyadRankingInstance) {
			double skill = computeSkillForDyad(d);
			skillForDyads.add(new Pair<Double, Dyad>(skill, d));
		}
		
		return new DyadRankingInstance(skillForDyads.stream().sorted((p1, p2) -> Double.compare(p1.getX(), p2.getX())).map(Pair::getY).collect(Collectors.toList()));
		
	}

	private double computeSkillForDyad(Dyad dyad) {
		Vector featureTransformVector = featureTransform.transform(dyad);
		double dot = w.dotProduct(featureTransformVector);
		double val = Math.exp(dot);
		log.debug("Feature transform for dyad {} is {}. \n Dot-Product is {} and skill is {}", dyad,
				featureTransformVector, dot, val);
		return val;
	}

	@Override
	public void train(@SuppressWarnings("rawtypes") IDataset dataset) throws TrainingException {
		if (!(dataset instanceof DyadRankingDataset)) {
			throw new IllegalArgumentException(
					"Can only train the feature transform Placket-Luce dyad ranker with a dyad ranking dataset!");
		}
		DyadRankingDataset dRDataset = (DyadRankingDataset) dataset;
		
		Map<IDyadRankingInstance, Map<Dyad, Vector>> featureTransforms = featureTransform.getPreComputedFeatureTransforms(dRDataset);
		negativeLogLikelihood.initialize(dRDataset, featureTransforms);
		negativeLogLikelihoodDerivative.initialize(dRDataset, featureTransforms);
		
		int alternativeLength = dRDataset.get(0).getDyadAtPosition(0).getAlternative().length();
		int instanceLength = dRDataset.get(0).getDyadAtPosition(0).getInstance().length();
		w = new DenseDoubleVector(
				featureTransform.getTransformedVectorLength(alternativeLength, instanceLength), 0.3);
		log.debug("Likelihood of the randomly filled w is {}", likelihoodOfParameter(w, dRDataset));
		BilinFunction fun = new BilinFunction(featureTransforms , dRDataset, featureTransform.getTransformedVectorLength(alternativeLength, instanceLength));
		QNMinimizer minimizer = new QNMinimizer();
		w = new DenseDoubleVector(minimizer.minimize(fun, 0.01, w.asArray()));
		log.debug("Finished optimizing, the final w is {}", w);

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
	private double likelihoodOfParameter(Vector w, DyadRankingDataset dataset) {
		int largeN = dataset.size();
		double outerProduct = 1.0;
		for (int smallN = 0; smallN < largeN; smallN++) {
			IDyadRankingInstance dyadRankingInstance = dataset.get(smallN);
			int mN = dyadRankingInstance.length();
			double innerProduct = 1.0;
			for (int m = 0; m < mN; m++) {
				Dyad dyad = dyadRankingInstance.getDyadAtPosition(m);
				Vector zNM = featureTransform.transform(dyad);
				double en = Math.exp(w.dotProduct(zNM));
				double denumSum = 0;
				for (int l = m; l < mN; l++) {
					Dyad dyadL = dyadRankingInstance.getDyadAtPosition(l);
					Vector zNL = featureTransform.transform(dyadL);
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
	public void setConfiguration(IPredictiveModelConfiguration configuration) throws ConfigurationException {
		/* Currently nothing to configure */
	}
}
