package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.TimeSeriesInstance;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ShapeletTransformAlgorithm.Shapelet;
import jaicore.ml.tsc.quality_measures.FStat;
import jaicore.ml.tsc.quality_measures.IQualityMeasure;
import jaicore.ml.tsc.util.TimeSeriesUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class ShapeletTransformTSClassifier
		extends TSClassifier<CategoricalAttributeType, CategoricalAttributeValue, TimeSeriesDataset> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShapeletTransformTSClassifier.class);

	private List<Shapelet> shapelets;
	private Classifier classifier;

	public ShapeletTransformTSClassifier(final int k, final int seed) {
		super(new ShapeletTransformAlgorithm(k, k / 2, new FStat(), seed));
	}

	public ShapeletTransformTSClassifier(final int k, final IQualityMeasure qm, final int seed) {
		super(new ShapeletTransformAlgorithm(k, k / 2, qm, seed));
	}


	public List<Shapelet> getShapelets() {
		return shapelets;
	}

	public void setShapelets(List<Shapelet> shapelets) {
		this.shapelets = shapelets;
	}

	@Override
	public CategoricalAttributeValue predict(TimeSeriesInstance instance) throws PredictionException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Prediction of single time series instances is not supported yet.");
	}

	@Override
	public List<CategoricalAttributeValue> predict(TimeSeriesDataset dataset) throws PredictionException {

		if(dataset.isMultivariate())
			LOGGER.warn(
					"Dataset to be predicted is multivariate but only first time series (univariate) will be considered.");
			
		LOGGER.debug("Transforming dataset...");
		TimeSeriesDataset transformedDataset = ShapeletTransformAlgorithm.shapeletTransform(dataset, this.shapelets);
		LOGGER.debug("Transformed dataset.");
		INDArray timeSeries = transformedDataset.getValuesOrNull(0);
		if (timeSeries == null)
			throw new IllegalArgumentException("Dataset matrix of the instances to be predicted must not be null!");

		LOGGER.debug("Converting time series dataset to Weka instances...");
		Instances insts = TimeSeriesUtil.timeSeriesDatasetToWekaInstances(transformedDataset);
		LOGGER.debug("Converted time series dataset to Weka instances.");
		
		LOGGER.debug("Starting prediction...");
		final List<CategoricalAttributeValue> predictions = new ArrayList<>();
		for (final Instance inst : insts) {
			try {
				double prediction = classifier.classifyInstance(inst);
				predictions.add((CategoricalAttributeValue) this.getTargetType()
						.buildAttributeValue(this.getTargetType().getDomain().get((int) prediction)));
			} catch (Exception e) {
				throw new PredictionException(String.format("Could not predict Weka instance {}.", inst.toString()), e);
			}
		}
		LOGGER.debug("Finished prediction.");

		return predictions;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
}
