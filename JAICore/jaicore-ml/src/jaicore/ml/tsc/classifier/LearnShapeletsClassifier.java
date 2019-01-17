package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.TimeSeriesInstance;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.core.exception.PredictionException;

public class LearnShapeletsClassifier
		extends TSClassifier<CategoricalAttributeType, CategoricalAttributeValue, TimeSeriesDataset> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LearnShapeletsClassifier.class);

	private double[][][] S;
	private double[][][] W;
	private double[] W_0;

	private int scaleR;
	private int K;
	private int minShapeLength;

	public LearnShapeletsClassifier(final int K, final double learningRate, final double regularization,
			final int scaleR, final int minShapeLength, final int maxIter, final int seed) {
		super(new LearnShapeletsAlgorithm(K, learningRate, regularization, scaleR, minShapeLength, maxIter, seed));

		this.scaleR = scaleR;
		this.K = K;
		this.minShapeLength = minShapeLength;
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

	@Override
	public CategoricalAttributeValue predict(TimeSeriesInstance instance) throws PredictionException {
		final HashMap<String, Double> scoring = new HashMap<>();
		String[] classes = (String[]) this.getTargetType().getDomain().toArray();

		// TODO: Improve this
		// INDArray instanceValues = instance.getAttributeValue(0,
		// TimeSeriesAttributeValue.class).getValue().getValue();
		double[] instanceValues = new double[0];
		int q = instanceValues.length;

		for (int i = 0; i < classes.length; i++) {
			double tmpScore = this.W_0[i];
			for (int r = 0; r < this.scaleR; r++) {
				for (int k = 0; k < this.K; k++) {
					tmpScore += LearnShapeletsAlgorithm.calculateM_hat(this.S, this.minShapeLength, r, instanceValues,
							k, q, LearnShapeletsAlgorithm.ALPHA) * W[i][r][k];
				}
			}
			scoring.put(classes[i], LearnShapeletsAlgorithm.sigmoid(tmpScore));
		}

		String predictedClass = Collections.max(scoring.entrySet(), Map.Entry.comparingByValue()).getKey();

		return (CategoricalAttributeValue) this.getTargetType().buildAttributeValue(predictedClass);
	}

	@Override
	public List<CategoricalAttributeValue> predict(TimeSeriesDataset dataset) throws PredictionException {

		final List<CategoricalAttributeValue> predictions = new ArrayList<>();

		if (dataset.isMultivariate())
			LOGGER.warn(
					"Dataset to be predicted is multivariate but only first time series (univariate) will be considered.");

		// INDArray timeSeries = dataset.getValuesOrNull(0);
		// TODO
		double[][] timeSeries = null;
		if (timeSeries == null)
			throw new IllegalArgumentException("Dataset matrix of the instances to be predicted must not be null!");

		List<String> classes = ((CategoricalAttributeType) dataset.getTargetType()).getDomain();

		LOGGER.debug("Starting prediction...");
		for (int inst = 0; inst < timeSeries.length; inst++) {
			// TODO
			double[] instanceValues = null;// TimeSeriesUtil.normalize(timeSeries[inst], true); //
			int q = instanceValues.length;

			final HashMap<String, Double> scoring = new HashMap<>();

			for (int i = 0; i < classes.size(); i++) {
				double tmpScore = this.W_0[i];
				for (int r = 0; r < this.scaleR; r++) {
					for (int k = 0; k < this.K; k++) {
						tmpScore += LearnShapeletsAlgorithm.calculateM_hat(this.S, this.minShapeLength, r,
								instanceValues, k, q, LearnShapeletsAlgorithm.ALPHA) * W[i][r][k];
					}
				}
				scoring.put(classes.get(i), LearnShapeletsAlgorithm.sigmoid(tmpScore));
			}
			LOGGER.debug("Scoring for instance {}: {}", inst, scoring);

			String predictedClass = Collections.max(scoring.entrySet(), Map.Entry.comparingByValue()).getKey();

			predictions.add((CategoricalAttributeValue) dataset.getTargetType().buildAttributeValue(predictedClass));
		}
		LOGGER.debug("Finished prediction.");

		return predictions;
	}
}
