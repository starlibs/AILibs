package jaicore.ml.tsc.classifier.shapelets;

import java.util.ArrayList;
import java.util.List;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.classifier.shapelets.ShapeletTransformLearningAlgorithm.IShapeletTransformLearningAlgorithmConfig;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.quality_measures.FStat;
import jaicore.ml.tsc.quality_measures.IQualityMeasure;
import jaicore.ml.tsc.shapelets.Shapelet;
import jaicore.ml.tsc.shapelets.search.AMinimumDistanceSearchStrategy;
import jaicore.ml.tsc.util.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class for a ShapeletTransform classifier as described in Jason Lines, Luke M.
 * Davis, Jon Hills, and Anthony Bagnall. 2012. A shapelet transform for time
 * series classification. In Proceedings of the 18th ACM SIGKDD international
 * conference on Knowledge discovery and data mining (KDD '12). ACM, New York,
 * NY, USA, 289-297.
 *
 * The classifier model is built of shapelets which are used for the
 * transformation of instances to the new feature space built by the shapelets
 * as dimensions. The feature values are the minimum distances of a time series
 * to the feature dimension's shapelet. An ensemble classifier trained on the
 * derived representation is then used for prediction.
 *
 * This classifier only supports univariate time series prediction.
 *
 * @author Julian Lienen
 *
 */
public class ShapeletTransformTSClassifier extends ASimplifiedTSClassifier<Integer> {
	/**
	 * Log4j logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ShapeletTransformTSClassifier.class);

	/**
	 * Model shapelets used as dimensions for the transformation.
	 */
	private List<Shapelet> shapelets;

	/**
	 * Weka ensemble classifier predicting the class based on the derived feature
	 * representation.
	 */
	private Classifier classifier;

	/**
	 * Strategy used for the minimum distance search.
	 */
	private AMinimumDistanceSearchStrategy minDistanceSearchStrategy;

	private final IShapeletTransformLearningAlgorithmConfig config;
	private final IQualityMeasure qualityMeasure;

	/**
	 * Constructs an Shapelet Transform classifier using <code>k</code> shapelets,
	 * </code>k/2</code> clusters of the shapelets after shapelet extraction and the
	 * {@link FStat} quality measure.
	 *
	 * @param k
	 *            Number of shapelets searched for and used as shapelet clustering
	 *            input
	 * @param seed
	 *            Seed for randomized operations
	 */
	public ShapeletTransformTSClassifier(final int k, final int seed) {
		this(k, new FStat(), seed, true);
	}

	/**
	 * Constructs an Shapelet Transform classifier using <code>k</code> shapelets,
	 * </code>k/2</code> clusters of the shapelets after shapelet extraction (if
	 * <code>clusterShapelets</code> is true and the quality measure function
	 * <code>qm</code>.
	 *
	 * @param k
	 *            Number of shapelets searched for and used as shapelet clustering
	 *            input if enabled
	 * @param qm
	 *            Quality measure function to be used to assess shapelets
	 * @param seed
	 *            See for randomized operations
	 * @param clusterShapelets
	 *            Indicator whether shapelet clustering should be used after
	 *            extracting the best k shapelets
	 */
	public ShapeletTransformTSClassifier(final int k, final IQualityMeasure qm, final int seed, final boolean clusterShapelets) {
		this(k, (k / 2), qm, seed, clusterShapelets, 3, 0, false, 1);
	}

	/**
	 * Constructs an Shapelet Transform classifier using <code>k</code> shapelets,
	 * </code>k/2</code> clusters of the shapelets after shapelet extraction (if
	 * <code>clusterShapelets</code> is true and the quality measure function
	 * <code>qm</code>.
	 *
	 * @param k
	 *            Number of shapelets searched for and used as shapelet clustering
	 *            input if enabled
	 * @param numClusters
	 *            Number of clusters into which the shapelets are clustered
	 * @param qm
	 *            Quality measure function to be used to assess shapelets
	 * @param seed
	 *            See for randomized operations
	 * @param clusterShapelets
	 *            Indicator whether shapelet clustering should be used after
	 *            extracting the best k shapelets
	 */
	public ShapeletTransformTSClassifier(final int k, final int numClusters, final IQualityMeasure qm, final int seed, final boolean clusterShapelets) {
		this(k, numClusters, qm, seed, clusterShapelets, 3, 0, false, 1);
	}

	/**
	 * Constructs an Shapelet Transform classifier using <code>k</code> shapelets,
	 * </code>k/2</code> clusters of the shapelets after shapelet extraction (if
	 * <code>clusterShapelets</code> is true and the quality measure function
	 * <code>qm</code>. <code>minShapeletLength</code> and
	 * <code>maxShapeletLength</code> specify the shapelet length borders, while
	 * <code>useHIVECOTEEnsemble</code> defines whether the HIVE COTE ensemble or
	 * the CAWPE ensemble should be used.
	 *
	 * @param k
	 *            Number of shapelets searched for and used as shapelet clustering
	 *            input if enabled
	 * @param qm
	 *            Quality measure function to be used to assess shapelets
	 * @param seed
	 *            See for randomized operations
	 * @param clusterShapelets
	 *            Indicator whether shapelet clustering should be used after
	 *            extracting the best k shapelets
	 * @param minShapeletLength
	 *            The minimal length of the shapelets
	 * @param maxShapeletLength
	 *            The maximal length of the shapelets
	 * @param useHIVECOTEEnsemble
	 *            Indicator whether the HIVE COTE ensemble should be used (CAWPE
	 *            otherwise)
	 * @param timeout
	 *            The timeout used for the training
	 * @param numFolds
	 *            See {@link ShapeletTransformLearningAlgorithm#numFolds}
	 */
	public ShapeletTransformTSClassifier(final int k, final int numClusters, final IQualityMeasure qm, final int seed, final boolean clusterShapelets, final int minShapeletLength, final int maxShapeletLength, final boolean useHIVECOTEEnsemble, final int numFolds) {
		super();
		this.config = ConfigCache.getOrCreate(IShapeletTransformLearningAlgorithmConfig.class);
		this.config.setProperty(IShapeletTransformLearningAlgorithmConfig.K_NUMSHAPELETS, "" + k);
		this.config.setProperty(IShapeletTransformLearningAlgorithmConfig.K_SEED, "" + seed);
		this.config.setProperty(IShapeletTransformLearningAlgorithmConfig.K_CLUSTERSHAPELETS, "" + clusterShapelets);
		this.config.setProperty(IShapeletTransformLearningAlgorithmConfig.K_SHAPELETLENGTH_MIN, "" + minShapeletLength);
		this.config.setProperty(IShapeletTransformLearningAlgorithmConfig.K_SHAPELETLENGTH_MAX, "" + maxShapeletLength);
		this.config.setProperty(IShapeletTransformLearningAlgorithmConfig.K_USEHIVECOTEENSEMBLE, "" + useHIVECOTEEnsemble);
		this.config.setProperty(IShapeletTransformLearningAlgorithmConfig.K_NUMFOLDS, "" + numFolds);
		this.config.setProperty(IShapeletTransformLearningAlgorithmConfig.K_NUMCLUSTERS, "" + numClusters);
		this.qualityMeasure = qm;
	}

	/**
	 * Getter for {@link ShapeletTransformTSClassifier#shapelets}.
	 *
	 * @return The actual list of shapelets used for the transformation
	 */
	public List<Shapelet> getShapelets() {
		return this.shapelets;
	}

	/**
	 * Setter for {@link ShapeletTransformTSClassifier#shapelets}.
	 *
	 * @param shapelets
	 *            The new list of shapelets to be set
	 */
	public void setShapelets(final List<Shapelet> shapelets) {
		this.shapelets = shapelets;
	}

	/**
	 * Setter for {@link ShapeletTransformTSClassifier#classifier}.
	 *
	 * @param classifier
	 *            The classifier to be set
	 */
	public void setClassifier(final Classifier classifier) {
		this.classifier = classifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer predict(final double[] univInstance) throws PredictionException {

		if (!this.isTrained()) {
			throw new PredictionException("Model has not been built before!");
		}

		double[] transformedInstance = ShapeletTransformLearningAlgorithm.shapeletTransform(univInstance, this.shapelets, this.minDistanceSearchStrategy);

		Instance inst = WekaUtil.simplifiedTSInstanceToWekaInstance(transformedInstance);

		try {
			return (int) Math.round(this.classifier.classifyInstance(inst));
		} catch (Exception e) {
			throw new PredictionException(String.format("Could not predict Weka instance {}.", inst.toString()), e);
		}
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
	public List<Integer> predict(final TimeSeriesDataset dataset) throws PredictionException {
		if (!this.isTrained()) {
			throw new PredictionException("Model has not been built before!");
		}

		// Multivariate support is not supported by the original paper's version
		if (dataset.isMultivariate()) {
			LOGGER.warn("Dataset to be predicted is multivariate but only first time series (univariate) will be considered.");
		}

		// Transforming the dataset using the extracted shapelets
		LOGGER.debug("Transforming dataset...");
		TimeSeriesDataset transformedDataset = null;
		try {
			transformedDataset = ShapeletTransformLearningAlgorithm.shapeletTransform(dataset, this.shapelets, null, -1, this.minDistanceSearchStrategy);
		} catch (InterruptedException e1) {
			throw new IllegalStateException("Got interrupted within the shapelet transform although it should not happen due to unlimited timeout.");
		}
		LOGGER.debug("Transformed dataset.");
		double[][] timeSeries = transformedDataset.getValuesOrNull(0);
		if (timeSeries == null) {
			throw new IllegalArgumentException("Dataset matrix of the instances to be predicted must not be null!");
		}

		// Prepare transformed Weka instances to let the ensemble predict
		LOGGER.debug("Converting time series dataset to Weka instances...");
		Instances insts = WekaUtil.simplifiedTimeSeriesDatasetToWekaInstances(transformedDataset);
		LOGGER.debug("Converted time series dataset to Weka instances.");

		// Prediction
		LOGGER.debug("Starting prediction...");
		final List<Integer> predictions = new ArrayList<>();
		for (final Instance inst : insts) {
			try {
				double prediction = this.classifier.classifyInstance(inst);
				predictions.add((int) Math.round(prediction));
			} catch (Exception e) {
				throw new PredictionException(String.format("Could not predict Weka instance {}.", inst.toString()), e);
			}
		}
		LOGGER.debug("Finished prediction.");

		return predictions;
	}

	/**
	 * Getter for {@link ShapeletTransformTSClassifier#minDistanceSearchStrategy}.
	 *
	 * @return the minDistanceSearchStrategy
	 */
	public AMinimumDistanceSearchStrategy getMinDistanceSearchStrategy() {
		return this.minDistanceSearchStrategy;
	}

	@Override
	public ShapeletTransformLearningAlgorithm getLearningAlgorithm(final TimeSeriesDataset dataset) {
		return new ShapeletTransformLearningAlgorithm(this.config, this, dataset, this.qualityMeasure);
	}
}
