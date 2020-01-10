package ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.shapelets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aeonbits.owner.ConfigCache;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.IOwnerBasedRandomizedAlgorithmConfig;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.ASimplifiedTSClassifier;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.shapelets.LearnShapeletsLearningAlgorithm.ILearnShapeletsLearningAlgorithmConfig;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.util.MathUtil;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.util.TimeSeriesUtil;

/**
 * <code>LearnShapeletsClassifier</code> published in "J. Grabocka, N.
 * Schilling, M. Wistuba, L. Schmidt-Thieme: Learning Time-Series Shapelets"
 * (https://www.ismll.uni-hildesheim.de/pub/pdfs/grabocka2014e-kdd.pdf).
 *
 * This classifier only supports univariate time series prediction.
 *
 * @author Julian Lienen
 *
 */
public class LearnShapeletsClassifier extends ASimplifiedTSClassifier<Integer> {

	/**
	 * The log4j logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(LearnShapeletsClassifier.class);

	/**
	 * The tensor storing the derived shapelets.
	 */
	private double[][][] s;
	/**
	 * The model's weights used for the class prediction learned by the training
	 * algorithm.
	 */
	private double[][][] w;
	/**
	 * The model's bias weights.
	 */
	private double[] w0;

	/**
	 * The number of classes.
	 */
	private int c;

	private final ILearnShapeletsLearningAlgorithmConfig config;

	/**
	 * Constructor of the {@link LearnShapeletsClassifier}.
	 *
	 * @param K
	 *            See {@link LearnShapeletsLearningAlgorithm#K}
	 * @param learningRate
	 *            See {@link LearnShapeletsLearningAlgorithm#learningRate}
	 * @param regularization
	 *            See {@link LearnShapeletsLearningAlgorithm#regularization}
	 * @param scaleR
	 *            See {@link LearnShapeletsLearningAlgorithm#scaleR}
	 * @param minShapeLengthPercentage
	 *            See {@link LearnShapeletsLearningAlgorithm#minShapeLengthPercentage}
	 * @param maxIter
	 *            See {@link LearnShapeletsLearningAlgorithm#maxIter}
	 * @param seed
	 *            See {@link LearnShapeletsLearningAlgorithm#seed}
	 * @param seed
	 *            See {@link LearnShapeletsLearningAlgorithm#timeout}
	 */
	public LearnShapeletsClassifier(final int K, final double learningRate, final double regularization, final int scaleR, final double minShapeLengthPercentage, final int maxIter, final int seed) {
		this(K, learningRate, regularization, scaleR, minShapeLengthPercentage, maxIter, 0.5, seed);
	}

	/**
	 * Constructor of the {@link LearnShapeletsClassifier}.
	 *
	 * @param K
	 *            See {@link LearnShapeletsLearningAlgorithm#K}
	 * @param learningRate
	 *            See {@link LearnShapeletsLearningAlgorithm#learningRate}
	 * @param regularization
	 *            See {@link LearnShapeletsLearningAlgorithm#regularization}
	 * @param scaleR
	 *            See {@link LearnShapeletsLearningAlgorithm#scaleR}
	 * @param minShapeLengthPercentage
	 *            See {@link LearnShapeletsLearningAlgorithm#minShapeLengthPercentage}
	 * @param maxIter
	 *            See {@link LearnShapeletsLearningAlgorithm#maxIter}
	 * @param seed
	 *            See {@link LearnShapeletsLearningAlgorithm#seed}
	 * @param gamma
	 *            See {@link LearnShapeletsLearningAlgorithm#gamma}
	 */
	public LearnShapeletsClassifier(final int K, final double learningRate, final double regularization, final int scaleR, final double minShapeLengthPercentage, final int maxIter, final double gamma, final int seed) {
		this.config = ConfigCache.getOrCreate(ILearnShapeletsLearningAlgorithmConfig.class);
		this.config.setProperty(ILearnShapeletsLearningAlgorithmConfig.K_NUMSHAPELETS, "" + K);
		this.config.setProperty(ILearnShapeletsLearningAlgorithmConfig.K_REGULARIZATION, "" + regularization);
		this.config.setProperty(ILearnShapeletsLearningAlgorithmConfig.K_SCALER, "" + scaleR);
		this.config.setProperty(ILearnShapeletsLearningAlgorithmConfig.K_SHAPELETLENGTH_RELMIN, "" + minShapeLengthPercentage);
		this.config.setProperty(IOwnerBasedRandomizedAlgorithmConfig.K_SEED, "" + seed);
		this.config.setProperty(ILearnShapeletsLearningAlgorithmConfig.K_MAXITER, "" + maxIter);
		this.config.setProperty(ILearnShapeletsLearningAlgorithmConfig.K_LEARNINGRATE, "" + learningRate);
		this.config.setProperty(ILearnShapeletsLearningAlgorithmConfig.K_GAMMA, "" + gamma);
	}

	/**
	 * Enables / disabled the parameter estimation of K within the training
	 * algorithm.
	 *
	 * @param estimateK
	 *            Value to be set
	 */
	public void setEstimateK(final boolean estimateK) {
		this.config.setProperty(ILearnShapeletsLearningAlgorithmConfig.K_ESTIMATEK, "" + estimateK);
	}

	/**
	 * @return {@link LearnShapeletsClassifier#s}.
	 */
	public double[][][] getS() {
		return this.s;
	}

	/**
	 * Setter for {@link LearnShapeletsClassifier#s}
	 *
	 * @param s
	 *            New value to be set
	 */
	public void setS(final double[][][] s) {
		this.s = s;
	}

	/**
	 * @return {@link LearnShapeletsClassifier#w}.
	 */
	public double[][][] getW() {
		return this.w;
	}

	/**
	 * Setter for {@link LearnShapeletsClassifier#w}
	 *
	 * @param w
	 *            New value to be set
	 */
	public void setW(final double[][][] w) {
		this.w = w;
	}

	/**
	 * @return {@link LearnShapeletsClassifier#w0}.
	 */
	public double[] getW0() {
		return this.w0;
	}

	/**
	 * Setter for {@link LearnShapeletsClassifier#w0}
	 *
	 * @param w0
	 *            New value to be set
	 */
	public void setW0(final double[] w0) {
		this.w0 = w0;
	}

	/**
	 * Setter for {@link LearnShapeletsClassifier#c}
	 *
	 * @param c
	 *            New value to be set
	 */
	public void setC(final int c) {
		this.c = c;
	}

	/**
	 * Setter for {@link LearnShapeletsClassifier#minShapeLength}
	 *
	 * @param minShapeLength
	 *            New value to be set
	 */
	public void setMinShapeLength(final int minShapeLength) {
		this.config.setProperty(ILearnShapeletsLearningAlgorithmConfig.K_SHAPELETLENGTH_MIN, "" + minShapeLength);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer predict(double[] univInstance) throws PredictionException {

		if (!this.isTrained()) {
			throw new PredictionException("Model has not been built before!");
		}

		final HashMap<Integer, Double> scoring = new HashMap<>();

		univInstance = TimeSeriesUtil.zNormalize(univInstance, LearnShapeletsLearningAlgorithm.USE_BIAS_CORRECTION);

		// Calculate target class according to the paper's section 5.3
		for (int i = 0; i < this.c; i++) {
			double tmpScore = this.w0[i];
			for (int r = 0; r < this.config.scaleR(); r++) {
				for (int k = 0; k < this.s[r].length; k++) {
					tmpScore += LearnShapeletsLearningAlgorithm.calculateMHat(this.s, this.config.minShapeletLength(), r, univInstance, k, univInstance.length, LearnShapeletsLearningAlgorithm.ALPHA) * this.w[i][r][k];
				}
			}
			scoring.put(i, MathUtil.sigmoid(tmpScore));
		}

		return Collections.max(scoring.entrySet(), Map.Entry.comparingByValue()).getKey();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer predict(final List<double[]> multivInstance) throws PredictionException {
		LOGGER.warn("Dataset to be predicted is multivariate but only first time series (univariate) will be considered.");

		return this.predict(multivInstance.get(0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> predict(final ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2 dataset) throws PredictionException {
		if (!this.isTrained()) {
			throw new PredictionException("Model has not been built before!");
		}

		if (dataset.isMultivariate()) {
			LOGGER.warn("Dataset to be predicted is multivariate but only first time series (univariate) will be considered.");
		}

		double[][] timeSeries = dataset.getValuesOrNull(0);
		if (timeSeries == null) {
			throw new IllegalArgumentException("Dataset matrix of the instances to be predicted must not be null!");
		}

		List<Integer> predictions = new ArrayList<>();

		LOGGER.debug("Starting prediction...");
		for (int inst = 0; inst < timeSeries.length; inst++) {
			double[] instanceValues = timeSeries[inst];
			predictions.add(this.predict(instanceValues));

		}
		LOGGER.debug("Finished prediction.");

		return predictions;
	}

	@Override
	public LearnShapeletsLearningAlgorithm getLearningAlgorithm(final TimeSeriesDataset2 dataset) {
		return new LearnShapeletsLearningAlgorithm(this.config, this, dataset);
	}
}
