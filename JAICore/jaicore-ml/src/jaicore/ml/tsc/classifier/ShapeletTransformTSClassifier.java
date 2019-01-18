package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ShapeletTransformAlgorithm.Shapelet;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.quality_measures.FStat;
import jaicore.ml.tsc.quality_measures.IQualityMeasure;
import jaicore.ml.tsc.util.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class ShapeletTransformTSClassifier
		extends ASimplifiedTSClassifier<Integer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShapeletTransformTSClassifier.class);

	private List<Shapelet> shapelets;
	private Classifier classifier;

	public ShapeletTransformTSClassifier(final int k, final int seed) {
		super(new ShapeletTransformAlgorithm(k, k / 2, new FStat(), seed, true));
	}
	
	public ShapeletTransformTSClassifier(final int k, final IQualityMeasure qm, final int seed,
			final boolean clusterShapelets) {
		super(new ShapeletTransformAlgorithm(k, k / 2, qm, seed, clusterShapelets));
	}

	public ShapeletTransformTSClassifier(final int k, final IQualityMeasure qm, final int seed,
			final boolean clusterShapelets, final int minShapeletLength, final int maxShapeletLength) {
		super(new ShapeletTransformAlgorithm(k, k / 2, qm, seed, clusterShapelets, minShapeletLength,
				maxShapeletLength));
	}


	public List<Shapelet> getShapelets() {
		return shapelets;
	}

	public void setShapelets(List<Shapelet> shapelets) {
		this.shapelets = shapelets;
	}

	// @Override
	// public List<CategoricalAttributeValue> predict(TimeSeriesDataset dataset)
	// throws PredictionException {
	//
	// if(dataset.isMultivariate())
	// LOGGER.warn(
	// "Dataset to be predicted is multivariate but only first time series
	// (univariate) will be considered.");
	//
	// LOGGER.debug("Transforming dataset...");
	// TimeSeriesDataset transformedDataset =
	// ShapeletTransformAlgorithm.shapeletTransform(dataset, this.shapelets);
	// LOGGER.debug("Transformed dataset.");
	// INDArray timeSeries = transformedDataset.getValuesOrNull(0);
	// if (timeSeries == null)
	// throw new IllegalArgumentException("Dataset matrix of the instances to be
	// predicted must not be null!");
	//
	// LOGGER.debug("Converting time series dataset to Weka instances...");
	// Instances insts =
	// WekaUtil.timeSeriesDatasetToWekaInstances(transformedDataset);
	// LOGGER.debug("Converted time series dataset to Weka instances.");
	//
	// LOGGER.debug("Starting prediction...");
	// final List<CategoricalAttributeValue> predictions = new ArrayList<>();
	// for (final Instance inst : insts) {
	// try {
	// double prediction = classifier.classifyInstance(inst);
	// predictions.add((CategoricalAttributeValue) this.getTargetType()
	// .buildAttributeValue(this.getTargetType().getDomain().get((int)
	// prediction)));
	// } catch (Exception e) {
	// throw new PredictionException(String.format("Could not predict Weka instance
	// {}.", inst.toString()), e);
	// }
	// }
	// LOGGER.debug("Finished prediction.");
	//
	// return predictions;
	// }

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	@Override
	public Integer predict(double[] univInstance) throws PredictionException {

		double[] transformedInstance = ShapeletTransformAlgorithm.shapeletTransform(univInstance, this.shapelets);

		Instance inst = WekaUtil.simplifiedTSInstanceToWekaInstance(transformedInstance);

		try {
			return (int) classifier.classifyInstance(inst);
		} catch (Exception e) {
			throw new PredictionException(String.format("Could not predict Weka instance {}.", inst.toString()), e);
		}
	}

	@Override
	public Integer predict(List<double[]> multivInstance) throws PredictionException {
		// TODO: Add multivariate support
		LOGGER.warn(
				"Dataset to be predicted is multivariate but only first time series (univariate) will be considered.");

		return predict(multivInstance.get(0));
	}

	@Override
	public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
		// TODO Auto-generated method stub
		

		if(dataset.isMultivariate())
			LOGGER.warn(
					"Dataset to be predicted is multivariate but only first time series (univariate) will be considered.");
			
		LOGGER.debug("Transforming dataset...");
		TimeSeriesDataset transformedDataset = ShapeletTransformAlgorithm.shapeletTransform(dataset, this.shapelets);
		LOGGER.debug("Transformed dataset.");
		double[][] timeSeries = transformedDataset.getValuesOrNull(0);
		if (timeSeries == null)
			throw new IllegalArgumentException("Dataset matrix of the instances to be predicted must not be null!");

		LOGGER.debug("Converting time series dataset to Weka instances...");
		Instances insts = WekaUtil.simplifiedTimeSeriesDatasetToWekaInstances(transformedDataset);
		LOGGER.debug("Converted time series dataset to Weka instances.");
		
		LOGGER.debug("Starting prediction...");
		final List<Integer> predictions = new ArrayList<>();
		for (final Instance inst : insts) {
			try {
				double prediction = classifier.classifyInstance(inst);
				predictions.add((int) Math.round(prediction));
			} catch (Exception e) {
				throw new PredictionException(String.format("Could not predict Weka instance {}.", inst.toString()), e);
			}
		}
		LOGGER.debug("Finished prediction.");

		return predictions;
	}
}
