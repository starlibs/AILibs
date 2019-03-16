package jaicore.ml.tsc.classifier.shapelets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.TimeOut;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.util.MathUtil;
import jaicore.ml.tsc.util.TimeSeriesUtil;

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
	private double[][][] S;
	/**
	 * The model's weights used for the class prediction learned by the training
	 * algorithm.
	 */
	private double[][][] W;
	/**
	 * The model's bias weights.
	 */
	private double[] W_0;

	/**
	 * The number of scales used for the shapelet lengths.
	 */
	private int scaleR;
	/**
	 * The minimum shapelet of the shapelets to be learned. Internally derived by
	 * the time series lengths and the <code>minShapeLengthPercentage</code>.
	 */
	private int minShapeLength;

	/**
	 * The number of classes.
	 */
	private int C;

	/**
	 * Constructor of the {@link LearnShapeletsClassifier}.
	 * 
	 * @param K
	 *            See {@link LearnShapeletsAlgorithm#K}
	 * @param learningRate
	 *            See {@link LearnShapeletsAlgorithm#learningRate}
	 * @param regularization
	 *            See {@link LearnShapeletsAlgorithm#regularization}
	 * @param scaleR
	 *            See {@link LearnShapeletsAlgorithm#scaleR}
	 * @param minShapeLengthPercentage
	 *            See {@link LearnShapeletsAlgorithm#minShapeLengthPercentage}
	 * @param maxIter
	 *            See {@link LearnShapeletsAlgorithm#maxIter}
	 * @param seed
	 *            See {@link LearnShapeletsAlgorithm#seed}
	 * @param seed
	 *            See {@link LearnShapeletsAlgorithm#timeout}
	 */
	public LearnShapeletsClassifier(final int K, final double learningRate, final double regularization,
			final int scaleR, final double minShapeLengthPercentage, final int maxIter, final int seed,
			final TimeOut timeout) {
		super(new LearnShapeletsAlgorithm(K, learningRate, regularization, scaleR, minShapeLengthPercentage, maxIter,
				seed, timeout));
		this.scaleR = scaleR;
	}

	/**
	 * Constructor of the {@link LearnShapeletsClassifier}.
	 * 
	 * @param K
	 *            See {@link LearnShapeletsAlgorithm#K}
	 * @param learningRate
	 *            See {@link LearnShapeletsAlgorithm#learningRate}
	 * @param regularization
	 *            See {@link LearnShapeletsAlgorithm#regularization}
	 * @param scaleR
	 *            See {@link LearnShapeletsAlgorithm#scaleR}
	 * @param minShapeLengthPercentage
	 *            See {@link LearnShapeletsAlgorithm#minShapeLengthPercentage}
	 * @param maxIter
	 *            See {@link LearnShapeletsAlgorithm#maxIter}
	 * @param seed
	 *            See {@link LearnShapeletsAlgorithm#seed}
	 */
	public LearnShapeletsClassifier(final int K, final double learningRate, final double regularization,
			final int scaleR, final double minShapeLengthPercentage, final int maxIter, final int seed) {
		super(new LearnShapeletsAlgorithm(K, learningRate, regularization, scaleR, minShapeLengthPercentage, maxIter,
				seed));
		this.scaleR = scaleR;
	}

	/**
	 * Enables / disabled the parameter estimation of K within the training
	 * algorithm.
	 * 
	 * @param estimateK
	 *            Value to be set
	 */
	public void setEstimateK(final boolean estimateK) {
		if (this.algorithm != null) {
			((LearnShapeletsAlgorithm) this.algorithm).setEstimateK(estimateK);
		} else {
			LOGGER.warn("Could not " + (estimateK ? "enable" : "disable")
					+ " estimation of parameter K due to non-set algorithm object in LearnShapeletsClassifier object.");
		}
	}

	/**
	 * @return {@link LearnShapeletsClassifier#S}.
	 */
	public double[][][] getS() {
		return S;
	}

	/**
	 * Setter for {@link LearnShapeletsClassifier#S}
	 * 
	 * @param S
	 *            New value to be set
	 */
	public void setS(double[][][] s) {
		S = s;
	}

	/**
	 * @return {@link LearnShapeletsClassifier#W}.
	 */
	public double[][][] getW() {
		return W;
	}

	/**
	 * Setter for {@link LearnShapeletsClassifier#W}
	 * 
	 * @param W
	 *            New value to be set
	 */
	public void setW(double[][][] w) {
		W = w;
	}

	/**
	 * @return {@link LearnShapeletsClassifier#W_0}.
	 */
	public double[] getW_0() {
		return W_0;
	}

	/**
	 * Setter for {@link LearnShapeletsClassifier#W_0}
	 * 
	 * @param W_0
	 *            New value to be set
	 */
	public void setW_0(double[] w_0) {
		W_0 = w_0;
	}

	/**
	 * Setter for {@link LearnShapeletsClassifier#C}
	 * 
	 * @param C
	 *            New value to be set
	 */
	public void setC(int c) {
		C = c;
	}

	/**
	 * Setter for {@link LearnShapeletsClassifier#minShapeLength}
	 * 
	 * @param minShapeLength
	 *            New value to be set
	 */
	public void setMinShapeLength(final int minShapeLength) {
		this.minShapeLength = minShapeLength;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer predict(double[] univInstance) throws PredictionException {

		if (!this.isTrained())
			throw new PredictionException("Model has not been built before!");

		final HashMap<Integer, Double> scoring = new HashMap<>();

		univInstance = TimeSeriesUtil.zNormalize(univInstance, LearnShapeletsAlgorithm.USE_BIAS_CORRECTION);

		// Calculate target class according to the paper's section 5.3
		for (int i = 0; i < this.C; i++) {
			double tmpScore = this.W_0[i];
			for (int r = 0; r < this.scaleR; r++) {
				for (int k = 0; k < this.S[r].length; k++) {
					tmpScore += LearnShapeletsAlgorithm.calculateM_hat(this.S, this.minShapeLength, r, univInstance, k,
							univInstance.length, LearnShapeletsAlgorithm.ALPHA) * W[i][r][k];
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
	public Integer predict(List<double[]> multivInstance) throws PredictionException {
		LOGGER.warn(
				"Dataset to be predicted is multivariate but only first time series (univariate) will be considered.");

		return predict(multivInstance.get(0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> predict(jaicore.ml.tsc.dataset.TimeSeriesDataset dataset) throws PredictionException {
		if (!this.isTrained())
			throw new PredictionException("Model has not been built before!");

		if (dataset.isMultivariate())
			LOGGER.warn(
					"Dataset to be predicted is multivariate but only first time series (univariate) will be considered.");

		double[][] timeSeries = dataset.getValuesOrNull(0);
		if (timeSeries == null)
			throw new IllegalArgumentException("Dataset matrix of the instances to be predicted must not be null!");

		List<Integer> predictions = new ArrayList<>();

		LOGGER.debug("Starting prediction...");
		for (int inst = 0; inst < timeSeries.length; inst++) {
			double[] instanceValues = timeSeries[inst];
			predictions.add(this.predict(instanceValues));

		}
		LOGGER.debug("Finished prediction.");

		return predictions;
	}
}
