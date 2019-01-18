package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.exception.PredictionException;

public class LearnShapeletsClassifier
		extends ASimplifiedTSClassifier<Integer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LearnShapeletsClassifier.class);

	private double[][][] S;
	private double[][][] W;
	private double[] W_0;

	private int scaleR;
	private int K;
	private int minShapeLength;

	private int C;

	public LearnShapeletsClassifier(final int K, final double learningRate, final double regularization,
			final int scaleR, final double minShapeLengthPercentage, final int maxIter, final int seed) {
		super(new LearnShapeletsAlgorithm(K, learningRate, regularization, scaleR, minShapeLengthPercentage, maxIter,
				seed));

		this.scaleR = scaleR;
		this.K = K;
	}

	public double[][][] getS() {
		return S;
	}

	public void setS(double[][][] s) {
		S = s;
	}

	public double[][][] getW() {
		return W;
	}

	public void setW(double[][][] w) {
		W = w;
	}

	public double[] getW_0() {
		return W_0;
	}

	public void setW_0(double[] w_0) {
		W_0 = w_0;
	}

	public void setC(int c) {
		C = c;
	}

	public void setMinShapeLength(final int minShapeLength) {
		this.minShapeLength = minShapeLength;
	}

	// @Override
	// public CategoricalAttributeValue predict(TimeSeriesInstance instance) throws
	// PredictionException {
	// final HashMap<String, Double> scoring = new HashMap<>();
	// String[] classes = (String[]) this.getTargetType().getDomain().toArray();
	//
	// // TODO: Improve this
	// // INDArray instanceValues = instance.getAttributeValue(0,
	// // TimeSeriesAttributeValue.class).getValue().getValue();
	// double[] instanceValues = new double[0];
	// int q = instanceValues.length;
	//
	// for (int i = 0; i < classes.length; i++) {
	// double tmpScore = this.W_0[i];
	// for (int r = 0; r < this.scaleR; r++) {
	// for (int k = 0; k < this.K; k++) {
	// tmpScore += LearnShapeletsAlgorithm.calculateM_hat(this.S,
	// this.minShapeLength, r, instanceValues,
	// k, q, LearnShapeletsAlgorithm.ALPHA) * W[i][r][k];
	// }
	// }
	// scoring.put(classes[i], LearnShapeletsAlgorithm.sigmoid(tmpScore));
	// }
	//
	// String predictedClass = Collections.max(scoring.entrySet(),
	// Map.Entry.comparingByValue()).getKey();
	//
	// return (CategoricalAttributeValue)
	// this.getTargetType().buildAttributeValue(predictedClass);
	// }
	//
	// @Override
	// public List<CategoricalAttributeValue> predict(TimeSeriesDataset dataset)
	// throws PredictionException {
	//
	// final List<CategoricalAttributeValue> predictions = new ArrayList<>();
	//
	// if (dataset.isMultivariate())
	// LOGGER.warn(
	// "Dataset to be predicted is multivariate but only first time series
	// (univariate) will be considered.");
	//
	// // INDArray timeSeries = dataset.getValuesOrNull(0);
	// // TODO
	// double[][] timeSeries = null;
	// if (timeSeries == null)
	// throw new IllegalArgumentException("Dataset matrix of the instances to be
	// predicted must not be null!");
	//
	// List<String> classes = ((CategoricalAttributeType)
	// dataset.getTargetType()).getDomain();
	//
	// LOGGER.debug("Starting prediction...");
	// for (int inst = 0; inst < timeSeries.length; inst++) {
	// // TODO
	// double[] instanceValues = null;// TimeSeriesUtil.normalize(timeSeries[inst],
	// true); //
	// int q = instanceValues.length;
	//
	// final HashMap<String, Double> scoring = new HashMap<>();
	//
	// for (int i = 0; i < classes.size(); i++) {
	// double tmpScore = this.W_0[i];
	// for (int r = 0; r < this.scaleR; r++) {
	// for (int k = 0; k < this.K; k++) {
	// tmpScore += LearnShapeletsAlgorithm.calculateM_hat(this.S,
	// this.minShapeLength, r,
	// instanceValues, k, q, LearnShapeletsAlgorithm.ALPHA) * W[i][r][k];
	// }
	// }
	// scoring.put(classes.get(i), LearnShapeletsAlgorithm.sigmoid(tmpScore));
	// }
	// LOGGER.debug("Scoring for instance {}: {}", inst, scoring);
	//
	// String predictedClass = Collections.max(scoring.entrySet(),
	// Map.Entry.comparingByValue()).getKey();
	//
	// predictions.add((CategoricalAttributeValue)
	// dataset.getTargetType().buildAttributeValue(predictedClass));
	// }
	// LOGGER.debug("Finished prediction.");
	//
	// return predictions;
	// }

	@Override
	public Integer predict(double[] univInstance) throws PredictionException {
		final HashMap<Integer, Double> scoring = new HashMap<>();

		for (int i = 0; i < this.C; i++) {
			double tmpScore = this.W_0[i];
			for (int r = 0; r < this.scaleR; r++) {
				for (int k = 0; k < this.S[r].length; k++) {
					tmpScore += LearnShapeletsAlgorithm.calculateM_hat(this.S, this.minShapeLength, r, univInstance, k,
							univInstance.length, LearnShapeletsAlgorithm.ALPHA) * W[i][r][k];
				}
			}
			scoring.put(i, LearnShapeletsAlgorithm.sigmoid(tmpScore));
		}

		return Collections.max(scoring.entrySet(), Map.Entry.comparingByValue()).getKey();
	}

	@Override
	public Integer predict(List<double[]> multivInstance) throws PredictionException {
		// TODO: Add multivariate support
		LOGGER.warn(
				"Dataset to be predicted is multivariate but only first time series (univariate) will be considered.");

		return predict(multivInstance.get(0));
	}

	@Override
	public List<Integer> predict(jaicore.ml.tsc.dataset.TimeSeriesDataset dataset) throws PredictionException {
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
