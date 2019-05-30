package jaicore.ml.tsc.classifier.shapelets;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.IRandomAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.classifier.ASimplifiedTSCLearningAlgorithm;
import jaicore.ml.tsc.classifier.ensemble.EnsembleProvider;
import jaicore.ml.tsc.classifier.ensemble.MajorityConfidenceVote;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.quality_measures.IQualityMeasure;
import jaicore.ml.tsc.shapelets.Shapelet;
import jaicore.ml.tsc.shapelets.search.AMinimumDistanceSearchStrategy;
import jaicore.ml.tsc.shapelets.search.EarlyAbandonMinimumDistanceSearchStrategy;
import jaicore.ml.tsc.util.TimeSeriesUtil;
import jaicore.ml.tsc.util.WekaUtil;
import weka.classifiers.Classifier;

/**
 * Algorithm training a ShapeletTransform classifier as described in Jason
 * Lines, Luke M. Davis, Jon Hills, and Anthony Bagnall. 2012. A shapelet
 * transform for time series classification. In Proceedings of the 18th ACM
 * SIGKDD international conference on Knowledge discovery and data mining (KDD
 * '12). ACM, New York, NY, USA, 289-297.
 *
 * @author Julian Lienen
 *
 */
public class ShapeletTransformLearningAlgorithm extends ASimplifiedTSCLearningAlgorithm<Integer, ShapeletTransformTSClassifier> {
	public interface IShapeletTransformLearningAlgorithmConfig extends IRandomAlgorithmConfig {

		public static final String K_NUMSHAPELETS = "numshapelets";
		public static final String K_NUMCLUSTERS = "numclusters";
		public static final String K_CLUSTERSHAPELETS = "clustershapelets";
		public static final String K_SHAPELETLENGTH_MIN = "minshapeletlength";
		public static final String K_SHAPELETLENGTH_MAX = "maxshapeletlength";
		public static final String K_USEHIVECOTEENSEMBLE = "usehivecoteensemble";
		public static final String K_ESTIMATESHAPELETLENGTHBORDERS = "estimateshapeletlengthborders";
		public static final String K_NUMFOLDS = "numfolds";

		/**
		 * Number of shapelets extracted in the shapelet search
		 */
		@Key(K_NUMSHAPELETS)
		@DefaultValue("10")
		public int numShapelets();

		/**
		 * Number of shapelet clusters when shapelet clustering is used.
		 */
		@Key(K_NUMCLUSTERS)
		@DefaultValue("10")
		public int numClusters();

		/**
		 * Indicator whether clustering of shapelets should be used.
		 */
		@Key(K_CLUSTERSHAPELETS)
		@DefaultValue("false")
		public boolean clusterShapelets();

		/**
		 * The minimum length of shapelets to be considered. Defaults to 3.
		 */
		@Key(K_SHAPELETLENGTH_MIN)
		@DefaultValue("3")
		public int minShapeletLength();

		/**
		 * The maximum length of shapelets to be considered.
		 */
		@Key(K_SHAPELETLENGTH_MAX)
		public int maxShapeletLength();

		/**
		 * Indicator whether the HIVE COTE ensemble should be used. If it is set to
		 * false, the CAWPE ensemble model will be used instead.
		 */
		@Key(K_USEHIVECOTEENSEMBLE)
		public boolean useHIVECOTEEnsemble();

		/**
		 * Indicator whether the min max estimation should be performed.
		 */
		@Key(K_ESTIMATESHAPELETLENGTHBORDERS)
		public boolean estimateShapeletLengthBorders();

		/**
		 * Number of folds used within the {@link MajorityConfidenceVote} scheme for the
		 * ensembles. Defaults to 5.
		 */
		@Key(K_NUMFOLDS)
		@DefaultValue("5")
		public int numFolds();
	}

	/**
	 * Log4j logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ShapeletTransformLearningAlgorithm.class);

	/**
	 * Quality measure function used to assess shapelets.
	 */
	private final IQualityMeasure qualityMeasure;

	/**
	 * Number of shapelets used for the min and max estimation as described in
	 * algorithm 4 of the original paper.
	 */
	private static final int MIN_MAX_ESTIMATION_SAMPLES = 10;

	/**
	 * Static indicator whether the bias (Bessel's) correction should be used.
	 */
	private static boolean USE_BIAS_CORRECTION = true;

	/**
	 * Strategy used for the minimum distance search.
	 */
	private AMinimumDistanceSearchStrategy minDistanceSearchStrategy = new EarlyAbandonMinimumDistanceSearchStrategy(USE_BIAS_CORRECTION);

	/**
	 * Constructs a training algorithm for the {@link ShapeletTransformTSClassifier}
	 * classifier specified by the given parameters.
	 *
	 * @param qualityMeasure
	 *            Quality measure used to assess the shapelets
	 */
	public ShapeletTransformLearningAlgorithm(final IShapeletTransformLearningAlgorithmConfig config, final ShapeletTransformTSClassifier classifier, final TimeSeriesDataset dataset, final IQualityMeasure qualityMeasure) {
		super(config, classifier, dataset);
		this.qualityMeasure = qualityMeasure;
	}

	/**
	 * Training procedure for {@link ShapeletTransformTSClassifier} using the
	 * training algorithm described in the paper.
	 *
	 * @return Returns the trained model
	 * @throws AlgorithmException
	 *             Thrown if the training could not be finished
	 */
	@Override
	public ShapeletTransformTSClassifier call() throws AlgorithmException {
		if (this.getNumCPUs() > 1) {
			LOGGER.warn("Multithreading is not supported for LearnShapelets yet. Therefore, the number of CPUs is not considered.");
		}
		long beginTime = System.currentTimeMillis();

		// Extract time series data and the corresponding targets
		TimeSeriesDataset data = this.getInput();
		if (data == null || data.isEmpty()) {
			throw new IllegalStateException("The time series input data must not be null or empty!");
		}
		if (data.isMultivariate()) {
			throw new UnsupportedOperationException("Multivariate datasets are not supported.");
		}

		final double[][] dataMatrix = data.getValuesOrNull(0);
		if (dataMatrix == null) {
			throw new IllegalArgumentException("Value matrix must be a valid 2D matrix containing the time series values for all instances!");
		}

		final int[] targetMatrix = data.getTargets();

		int minShapeletLength = this.getConfig().minShapeletLength();
		int maxShapeletLength = this.getConfig().maxShapeletLength();
		int seed = this.getConfig().seed();
		ShapeletTransformTSClassifier model = this.getClassifier();

		try {
			final int timeSeriesLength = dataMatrix[0].length;

			// Estimate min and max
			if (this.getConfig().estimateShapeletLengthBorders()) {
				LOGGER.debug("Starting min max estimation.");
				int[] minMax = this.estimateMinMax(dataMatrix, targetMatrix, beginTime);
				minShapeletLength = minMax[0];
				maxShapeletLength = minMax[1];
				LOGGER.debug("Finished min max estimation. min={}, max={}", minShapeletLength, maxShapeletLength);
			} else {
				if (maxShapeletLength == -1) {
					maxShapeletLength = timeSeriesLength - 1;
				}
			}

			if (maxShapeletLength >= timeSeriesLength) {
				LOGGER.debug("The maximum shapelet length was larger than the total time series length. Therefore, it will be set to time series length - 1.");
				maxShapeletLength = timeSeriesLength - 1;
			}

			// Determine shapelets
			LOGGER.debug("Starting cached shapelet selection with min={}, max={} and k={}...", minShapeletLength, maxShapeletLength, this.getConfig().numShapelets());
			List<Shapelet> shapelets = null;

			shapelets = this.shapeletCachedSelection(dataMatrix,minShapeletLength, maxShapeletLength, this.getConfig().numShapelets(), targetMatrix, beginTime);
			LOGGER.debug("Finished cached shapelet selection. Extracted {} shapelets.", shapelets.size());

			// Cluster shapelets
			if (this.getConfig().clusterShapelets()) {
				LOGGER.debug("Starting shapelet clustering...");
				shapelets = this.clusterShapelets(shapelets, this.getConfig().numClusters(), beginTime);
				LOGGER.debug("Finished shapelet clustering. Staying with {} shapelets.", shapelets.size());
			}
			model.setShapelets(shapelets);

			// Transforming the data using the extracted shapelets
			LOGGER.debug("Transforming the training data using the extracted shapelets.");
			TimeSeriesDataset transfTrainingData = shapeletTransform(data, model.getShapelets(), this.getTimeout(), beginTime, this.minDistanceSearchStrategy);
			LOGGER.debug("Finished transforming the training data.");

			// Inititalize Weka ensemble
			LOGGER.debug("Initializing ensemble classifier...");
			Classifier classifier = null;
			try {
				classifier = this.getConfig().useHIVECOTEEnsemble() ? EnsembleProvider.provideHIVECOTEEnsembleModel(seed, this.getConfig().numFolds()) : EnsembleProvider.provideCAWPEEnsembleModel(seed, this.getConfig().numFolds());
			} catch (Exception e1) {
				throw new AlgorithmException(e1, "Could not train model due to ensemble exception.");
			}
			LOGGER.debug("Initialized ensemble classifier.");

			// Train Weka ensemble using the data
			LOGGER.debug("Starting ensemble training...");
			try {
				WekaUtil.buildWekaClassifierFromSimplifiedTS(classifier, transfTrainingData);
			} catch (TrainingException e) {
				throw new AlgorithmException(e, "Could not train classifier due to a training exception.");
			}
			LOGGER.debug("Finished ensemble training.");

			model.setClassifier(classifier);
		} catch (InterruptedException e1) {
			LOGGER.warn("Timeout in training Shapelet Transform classifier. Aborting...");
			throw new AlgorithmException("Could not finish training due to timeout.");
		}

		return model;
	}

	/**
	 * Implements the min max estimation (algorithm 4 in the paper) for an initial
	 * value used as parameters for the shapelet selection.
	 *
	 * @param data
	 *            Input data which is sampled from
	 * @param classes
	 *            Classes of the input data instances
	 * @param beginTime
	 *            Start timer used for the timeout checks
	 * @return Returns an int[] object of length 2 storing the min (index 0) and the
	 *         max (index 1) estimation
	 * @throws InterruptedException
	 */
	private int[] estimateMinMax(final double[][] data, final int[] classes, final long beginTime) throws InterruptedException {
		int[] result = new int[2];

		long numInstances = data.length;

		List<Shapelet> shapelets = new ArrayList<>();
		for (int i = 0; i < MIN_MAX_ESTIMATION_SAMPLES; i++) {
			double[][] tmpMatrix = new double[MIN_MAX_ESTIMATION_SAMPLES][data[0].length];
			Random rand = new Random(this.getConfig().seed());
			int[] tmpClasses = new int[MIN_MAX_ESTIMATION_SAMPLES];
			for (int j = 0; j < MIN_MAX_ESTIMATION_SAMPLES; j++) {
				int nextIndex = (int) (rand.nextInt() % numInstances);
				if (nextIndex < 0) {
					nextIndex += numInstances;
				}
				for (int k = 0; k < data[0].length; k++) {
					tmpMatrix[j][k] = data[nextIndex][k];
				}
				tmpClasses[j] = classes[nextIndex];
			}

			shapelets.addAll(this.shapeletCachedSelection(tmpMatrix, 3, data[0].length, 10, tmpClasses, beginTime));
		}

		Shapelet.sortByLengthAsc(shapelets);

		LOGGER.debug("Number of shapelets found in min/max estimation: {}", shapelets.size());

		// Min
		result[0] = shapelets.get(25).getLength();
		// Max
		result[1] = shapelets.get(75).getLength();

		return result;
	}

	/**
	 * Clusters the given <code>shapelets</code> into <code>noClusters</code>
	 * clusters (cf. algorithm 6 of the original paper).
	 *
	 * @param shapelets
	 *            Shapelets to be clustered.
	 * @param noClusters
	 *            Number of clusters to be used, i. e. the size of the output list
	 * @param beginTime
	 *            Begin time of the training execution used for the timeout checks
	 * @return Returns the clustered shapelets
	 * @throws InterruptedException
	 *             Thrown when a timeout occurred
	 */
	public List<Shapelet> clusterShapelets(final List<Shapelet> shapelets, final int noClusters, final long beginTime) throws InterruptedException {
		final List<List<Shapelet>> C = new ArrayList<>();
		for (final Shapelet shapelet : shapelets) {
			List<Shapelet> list = new ArrayList<>();
			list.add(shapelet);
			C.add(list);
		}

		// Get clusters
		while (C.size() > noClusters) {
			if ((System.currentTimeMillis() - beginTime) > this.getTimeout().milliseconds()) {
				throw new InterruptedException("Interrupted training due to timeout.");
			}

			INDArray M = Nd4j.create(C.size(), C.size());
			for (int i = 0; i < C.size(); i++) {
				for (int j = 0; j < C.size(); j++) {
					double distance = 0;
					int comparisons = C.get(i).size() * C.get(j).size();
					for (int l = 0; l < C.get(i).size(); l++) {
						for (int k = 0; k < C.get(j).size(); k++) {
							Shapelet c_l = C.get(i).get(l);
							Shapelet c_k = C.get(j).get(k);

							if (c_l.getLength() > c_k.getLength()) {
								distance += this.minDistanceSearchStrategy.findMinimumDistance(c_k, c_l.getData());
							} else {
								distance += this.minDistanceSearchStrategy.findMinimumDistance(c_l, c_k.getData());
							}
						}
					}

					M.putScalar(new int[] { i, j }, distance / comparisons);
				}
			}

			double best = Double.MAX_VALUE;
			int x = 0;
			int y = 0;
			for (int i = 0; i < M.shape()[0]; i++) {
				for (int j = 0; j < M.shape()[1]; j++) {
					if (M.getDouble(i, j) < best && i != j) {
						x = i;
						y = j;
						best = M.getDouble(i, j);
					}
				}
			}
			final List<Shapelet> C_prime = C.get(x);
			C_prime.addAll(C.get(y));
			Shapelet maxClusterShapelet = Shapelet.getHighestQualityShapeletInList(C_prime);
			if (x > y) {
				C.remove(x);
				C.remove(y);
			} else {
				C.remove(y);
				C.remove(x);
			}
			C.add(Arrays.asList(maxClusterShapelet));
		}

		// Flatten list
		return C.stream().flatMap(List::stream).collect(Collectors.toList());
	}

	/**
	 * Function implementing the shapelet cached selection described in algorithm 3
	 * in the original paper. The function searches for the best k shapelets based
	 * on the quality measure {@link ShapeletTransformLearningAlgorithm#qualityMeasure}.
	 *
	 * @param data
	 *            The training data which is used for cache extraction and
	 *            evaluation
	 * @param min
	 *            The minimal length of the shapelets
	 * @param max
	 *            The maximal length of the shapelets
	 * @param k
	 *            The number of shapelets to be kept
	 * @param classes
	 *            The classes of the instances
	 * @param beginTime
	 *            Begin time of the training execution used for the timeout checks
	 * @return Returns the k best shapelets found in the search procedure
	 * @throws InterruptedException
	 *             Thrown when a timeout occurred
	 */
	private List<Shapelet> shapeletCachedSelection(final double[][] data, final int min, final int max, final int k, final int[] classes, final long beginTime) throws InterruptedException {
		List<Map.Entry<Shapelet, Double>> kShapelets = new ArrayList<>();

		final int numInstances = data.length;

		for (int i = 0; i < numInstances; i++) {

			if ((System.currentTimeMillis() - beginTime) > this.getTimeout().milliseconds()) {
				throw new InterruptedException("Interrupted training due to timeout.");
			}

			List<Map.Entry<Shapelet, Double>> shapelets = new ArrayList<>();
			for (int l = min; l < max; l++) {
				Set<Shapelet> W_il = generateCandidates(data[i], l, i);
				for (Shapelet s : W_il) {
					List<Double> D_s = this.findDistances(s, data);
					double quality = this.qualityMeasure.assessQuality(D_s, classes);
					s.setDeterminedQuality(quality);
					shapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(s, quality));
				}
			}
			sortByQualityDesc(shapelets);

			shapelets = removeSelfSimilar(shapelets);
			kShapelets = merge(k, kShapelets, shapelets);
		}

		return kShapelets.stream().map(entry -> entry.getKey()).collect(Collectors.toList());
	}

	/**
	 * Function merging shapelet lists based on their quality scores. Only the k
	 * best shapelets of the union of both lists are retained.
	 *
	 * @param k
	 *            Number of elements to be retained
	 * @param kShapelets
	 *            The previous best shapelets
	 * @param shapelets
	 *            The new shapelets to be added to the top-k list
	 * @return Returns the list containing the k best shapelets only
	 */
	public static List<Map.Entry<Shapelet, Double>> merge(final int k, final List<Map.Entry<Shapelet, Double>> kShapelets, final List<Map.Entry<Shapelet, Double>> shapelets) {

		kShapelets.addAll(shapelets);

		// Retain only k
		sortByQualityDesc(kShapelets);
		int numRemoveItems = kShapelets.size() - k;
		for (int i = 0; i < numRemoveItems; i++) {
			kShapelets.remove(kShapelets.size() - 1);
		}

		return kShapelets;
	}

	/**
	 * Sorts a list of shapelets together with their quality values descending based
	 * on the qualities.
	 *
	 * @param list
	 *            The list to be sorted in place
	 */
	private static void sortByQualityDesc(final List<Map.Entry<Shapelet, Double>> list) {
		list.sort((e1, e2) -> (-1) * e1.getValue().compareTo(e2.getValue()));
	}

	/**
	 * Function removing self-similar shapelets from a list storing shapelet and
	 * their quality entries. See
	 * {@link ShapeletTransformLearningAlgorithm#isSelfSimilar(Shapelet, Shapelet)}.
	 *
	 * @param shapelets
	 *            Shapelets to be compared
	 * @return A sublist from the given <code>shapelets</code> list which does not
	 *         contain any self-similar shapelets
	 */
	public static List<Map.Entry<Shapelet, Double>> removeSelfSimilar(final List<Map.Entry<Shapelet, Double>> shapelets) {
		List<Map.Entry<Shapelet, Double>> result = new ArrayList<>();
		for (final Map.Entry<Shapelet, Double> entry : shapelets) {
			// Check whether there is already a self similar shapelet in the result list
			boolean selfSimilarExisting = false;
			for (final Map.Entry<Shapelet, Double> s : result) {
				if (isSelfSimilar(entry.getKey(), s.getKey())) {
					selfSimilarExisting = true;
				}
			}

			if (!selfSimilarExisting) {
				result.add(entry);
			}
		}

		return result;
	}

	/**
	 * Function checking whether the two given shapelets are self-similar, i. e. if
	 * their indices overlap. Assumes that both shapelets are from the same time
	 * series.
	 *
	 * @param s1
	 *            First shapelet to be compared
	 * @param s2
	 *            Second shapelet to be compared
	 * @return Returns whether the indices of the given shapelets overlap
	 */
	private static boolean isSelfSimilar(final Shapelet s1, final Shapelet s2) {
		if (s1.getInstanceIndex() == s2.getInstanceIndex()) {
			return (s1.getStartIndex() < (s2.getStartIndex() + s2.getLength())) && (s2.getStartIndex() < (s1.getStartIndex() + s1.getLength()));
		} else {
			return false;
		}
	}

	/**
	 * Function finding the minimum single squared Euclidean distance for each
	 * instance among all of its subsequences compared to the shapelet
	 * <code>s</code>.
	 *
	 * @param s
	 *            Shapelet which is compared to the instances
	 * @param matrix
	 *            Matrix storing the data instance vectors
	 * @return Returns the list of all minimum distances of the shapelet and all the
	 *         instances
	 */
	public List<Double> findDistances(final Shapelet s, final double[][] matrix) {
		List<Double> result = new ArrayList<>();

		for (int i = 0; i < matrix.length; i++) {
			result.add(this.minDistanceSearchStrategy.findMinimumDistance(s, matrix[i]));
		}

		return result;
	}

	/**
	 * Function generation shapelet candidates for a given instance vector
	 * <code>data</code>, the length <code>l</code> and the candidate index which is
	 * used to identify the source of the shapelet's data.
	 *
	 * @param data
	 *            Data vector from which the values are extracted
	 * @param l
	 *            The length of the generated candidate shapelet
	 * @param candidateIndex
	 *            Instance index which is used to identify the generated shapelets
	 * @return Returns a set of shapelet candidates with the length <code>l</code>
	 */
	public static Set<Shapelet> generateCandidates(final double[] data, final int l, final int candidateIndex) {
		Set<Shapelet> result = new HashSet<>();

		for (int i = 0; i < data.length - l + 1; i++) {
			double[] tmpData = TimeSeriesUtil.getInterval(data, i, i + l);

			result.add(new Shapelet(TimeSeriesUtil.zNormalize(tmpData, USE_BIAS_CORRECTION), i, l, candidateIndex));
		}
		return result;
	}

	/**
	 * Performs a shapelet transform on a complete <code>dataSet</code>. See
	 * {@link ShapeletTransformLearningAlgorithm#shapeletTransform(double[], List)}.
	 *
	 * @param dataSet
	 *            Data set to be transformed
	 * @param shapelets
	 *            Shapelets used as new feature dimensions
	 * @param timeout
	 *            Timeout compared to the current time difference to the
	 *            <code>beginTime</code>
	 * @param beginTime
	 *            System time in ms when the training algorithm has started
	 * @param searchStrategy
	 *            Search strategy used to find the minimum distance from a shapelet
	 *            to the time series
	 * @return Returns the transformed data set
	 * @throws InterruptedException
	 *             Thrown if there was a timeout
	 */
	public static TimeSeriesDataset shapeletTransform(final TimeSeriesDataset dataSet, final List<Shapelet> shapelets, final TimeOut timeout, final long beginTime, final AMinimumDistanceSearchStrategy searchStrategy)
			throws InterruptedException {
		// Since the original paper only works on univariate data, this is assumed to be
		// the case
		if (dataSet.isMultivariate()) {
			throw new UnsupportedOperationException("Multivariate datasets are not supported yet!");
		}

		double[][] timeSeries = dataSet.getValuesOrNull(0);
		if (timeSeries == null) {
			throw new IllegalArgumentException("Time series matrix must be a valid 2d matrix!");
		}

		double[][] transformedTS = new double[timeSeries.length][];

		for (int i = 0; i < timeSeries.length; i++) {
			if (timeout != null && (System.currentTimeMillis() - beginTime) > timeout.milliseconds()) {
				throw new InterruptedException("Interrupted training due to timeout.");
			}

			transformedTS[i] = shapeletTransform(timeSeries[i], shapelets, searchStrategy);
		}

		dataSet.replace(0, transformedTS, dataSet.getTimestampsOrNull(0));
		return dataSet;

	}

	/**
	 * Function transforming the given <code>instance</code> into the new feature
	 * space spanned by the shapelets. Uses the minimum squared Euclidean distance
	 * of the corresponding shapelets to the instance as feature values.
	 *
	 * @param instance
	 *            The instance to be transformed
	 * @param shapelets
	 *            The shapelets to be used as new feature dimensions
	 * @param searchStrategy
	 *            Search strategy used to find the minimum distance from a shapelet
	 *            to the time series
	 * @return Returns the transformed instance feature vector
	 */
	public static double[] shapeletTransform(final double[] instance, final List<Shapelet> shapelets, final AMinimumDistanceSearchStrategy searchStrategy) {

		double[] transformedTS = new double[shapelets.size()];

		for (int j = 0; j < shapelets.size(); j++) {
			transformedTS[j] = searchStrategy.findMinimumDistance(shapelets.get(j), instance);
		}

		return transformedTS;
	}

	/**
	 * Getter for {@link ShapeletTransformLearningAlgorithm#minDistanceSearchStrategy}.
	 *
	 * @return the minDistanceSearchStrategy
	 */
	public AMinimumDistanceSearchStrategy getMinDistanceSearchStrategy() {
		return this.minDistanceSearchStrategy;
	}

	/**
	 * Setter for {@link ShapeletTransformLearningAlgorithm#minDistanceSearchStrategy}.
	 *
	 * @param minDistanceSearchStrategy
	 *            the minDistanceSearchStrategy to set
	 */
	public void setMinDistanceSearchStrategy(final AMinimumDistanceSearchStrategy minDistanceSearchStrategy) {
		this.minDistanceSearchStrategy = minDistanceSearchStrategy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerListener(final Object listener) {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlgorithmEvent nextWithException() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IShapeletTransformLearningAlgorithmConfig getConfig() {
		return (IShapeletTransformLearningAlgorithmConfig)super.getConfig();
	}
}
