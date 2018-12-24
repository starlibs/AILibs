package jaicore.ml.dyadranking.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.ConfigurationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.core.optimizing.IGradientBasedOptimizer;
import jaicore.ml.core.optimizing.graddesc.GradientDescentOptimizer;
import jaicore.ml.core.optimizing.lbfgs.LBFGSOptimizerWrapper;
import jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
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

	private static final Logger log = LoggerFactory.getLogger(APLDyadRanker.class);

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

	/* The optimizer used to find w */
	private IGradientBasedOptimizer optimizer = new LBFGSOptimizerWrapper();

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
		TreeMap<Double, List<Dyad>> ordering = new TreeMap<>();

		dyadRankingInstance.forEach(dyad -> {
			double skill = computeSkillForDyad(dyad);
			if (ordering.containsKey(skill)) {
				ordering.get(skill).add(dyad);
			} else {
				ordering.put(skill, new ArrayList<Dyad>());
				ordering.get(skill).add(dyad);
			}
		});

		return new DyadRankingInstance(new ArrayList<Dyad>(
				ordering.descendingMap().values().stream().flatMap(List::stream).collect(Collectors.toList())));
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
	public void train(IDataset dataset) throws TrainingException {
		if (!(dataset instanceof DyadRankingDataset)) {
			throw new IllegalArgumentException(
					"Can only train the feature transform Placket-Luce dyad ranker with a dyad ranking dataset!");
		}
		DyadRankingDataset dRDataset = (DyadRankingDataset) dataset;

		negativeLogLikelihood.initialize(dRDataset, featureTransform);
		negativeLogLikelihoodDerivative.initialize(dRDataset, featureTransform);
		int alternativeLength = ((IDyadRankingInstance) dRDataset.get(0)).getDyadAtPosition(0).getAlternative()
				.length();
		int instanceLength = ((IDyadRankingInstance) dRDataset.get(0)).getDyadAtPosition(0).getInstance().length();
		Vector initialGuess = new DenseDoubleVector(
				featureTransform.getTransformedVectorLength(alternativeLength, instanceLength), -0.3);
		// initialGuess.fillRandomly();
		System.out.println("likelihood of this the random w is " + likelihoodOfParameter(initialGuess, dRDataset));
		System.out.println(negativeLogLikelihoodDerivative.apply(initialGuess));
		w = optimizer.optimize(negativeLogLikelihood, negativeLogLikelihoodDerivative, initialGuess);
		System.out.println("log lilkelihood is " + negativeLogLikelihood.apply(w));
		System.out.println("w is " + w);
		System.out.println("derivative of w is " + negativeLogLikelihoodDerivative.apply(w).toString());
		log.debug("Finished training the ranker. W-Vector is {}", w);
		System.out.println("likelihood of this w is " + likelihoodOfParameter(w, dRDataset));

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
		int N = dataset.size();
		double outerProduct = 1.0;
		for (int n = 0; n < N; n++) {
			IDyadRankingInstance dyadRankingInstance = (IDyadRankingInstance) dataset.get(n);
			int M_n = dyadRankingInstance.length();
			float innerProduct = 1.0f;
			for (int m = 0; m < M_n; m++) {
				Dyad dyad = dyadRankingInstance.getDyadAtPosition(m);
				Vector z_nm = featureTransform.transform(dyad);
				float en = (float) Math.exp(w.dotProduct(z_nm));
				float denum_sum = 0;
				for (int l = m; l < M_n; l++) {
					Dyad dyad_l = dyadRankingInstance.getDyadAtPosition(l);
					Vector z_nl = featureTransform.transform(dyad_l);
					denum_sum += Math.exp(w.dotProduct(z_nl));
				}
				innerProduct = innerProduct * (en / denum_sum);
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
