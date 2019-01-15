package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.TimeSeriesInstance;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeValue;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.util.TimeSeriesUtil;

public class LearnShapeletsClassifier
		extends TSClassifier<CategoricalAttributeType, CategoricalAttributeValue, TimeSeriesDataset> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LearnShapeletsClassifier.class);

	private List<INDArray> S;
	private INDArray W;
	private INDArray W_0;

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

	public List<INDArray> getS() {
		return S;
	}

	public void setS(List<INDArray> s) {
		S = s;
	}

	public INDArray getW() {
		return W;
	}

	public void setW(INDArray w) {
		W = w;
	}

	public INDArray getW_0() {
		return W_0;
	}

	public void setW_0(INDArray w_0) {
		W_0 = w_0;
	}

	@Override
	public CategoricalAttributeValue predict(TimeSeriesInstance instance) throws PredictionException {
		final HashMap<String, Double> scoring = new HashMap<>();
		String[] classes = (String[]) this.getTargetType().getDomain().toArray();

		// TODO: Improve this
		INDArray instanceValues = instance.getAttributeValue(0, TimeSeriesAttributeValue.class).getValue().getValue();
		int q = (int) instanceValues.length();

		for (int i = 0; i < classes.length; i++) {
			double tmpScore = this.W_0.getDouble(i);
			for (int r = 0; r < this.scaleR; r++) {
				for (int k = 0; k < this.K; k++) {
					tmpScore += LearnShapeletsAlgorithm.calculateM_hat(this.S, this.minShapeLength, r, instanceValues,
							k, q, LearnShapeletsAlgorithm.ALPHA) * W.getDouble(i, r, k);
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

		INDArray timeSeries = dataset.getValuesOrNull(0);
		if (timeSeries == null)
			throw new IllegalArgumentException("Dataset matrix of the instances to be predicted must not be null!");

		List<String> classes = ((CategoricalAttributeType) dataset.getTargetType()).getDomain();

		LOGGER.debug("Starting prediction...");
		for (int inst = 0; inst < timeSeries.shape()[0]; inst++) {
			INDArray instanceValues = TimeSeriesUtil.normalizeINDArray(timeSeries.getRow(inst), true); //
			int q = (int) instanceValues.length();

			final HashMap<String, Double> scoring = new HashMap<>();

			for (int i = 0; i < classes.size(); i++) {
				double tmpScore = this.W_0.getDouble(i);
				for (int r = 0; r < this.scaleR; r++) {
					for (int k = 0; k < this.K; k++) {
						tmpScore += LearnShapeletsAlgorithm.calculateM_hat(this.S, this.minShapeLength, r,
								instanceValues, k, q, LearnShapeletsAlgorithm.ALPHA) * W.getDouble(i, r, k);
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
