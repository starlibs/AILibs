package jaicore.ml.tsc.classifier;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.quality_measures.IQualityMeasure;
import jaicore.ml.tsc.shapelets.Shapelet;
import jaicore.ml.tsc.util.MathUtil;
import jaicore.ml.tsc.util.TimeSeriesUtil;
import jaicore.ml.tsc.util.WekaUtil;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.RotationForest;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.EuclideanDistance;
import weka.core.SelectedTag;

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
public class ShapeletTransformAlgorithm extends ASimplifiedTSCAlgorithm<Integer, ShapeletTransformTSClassifier> {

	/**
	 * Log4j logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ShapeletTransformAlgorithm.class);

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
	 * Number of shapelets to be kept and later used for the transformation.
	 */
	private int k;

	/**
	 * Seed used for randomized operations.
	 */
	private final int seed;

	/**
	 * Number of shapelet clusters when shapelet clustering is used.
	 */
	private final int noClusters;

	/**
	 * Indicator whether clustering of shapelets should be used.
	 */
	private boolean clusterShapelets;
	/**
	 * Indicator whether the min max estimation should be performed.
	 */
	private boolean estimateShapeletLengthBorders;

	/**
	 * The minimum length of shapelets to be considered.
	 */
	private int minShapeletLength;

	/**
	 * The maximum length of shapelets to be considered.
	 */
	private int maxShapeletLength;

	/**
	 * Static indicator whether the bias (Bessel's) correction should be used.
	 */
	private static boolean USE_BIAS_CORRECTION = true;

	/**
	 * See {@link IAlgorithm#getTimeout()}.
	 */
	private TimeOut timeout = new TimeOut(Integer.MAX_VALUE, TimeUnit.SECONDS);

	/**
	 * Indicator whether the HIVE COTE ensemble should be used. If it is set to
	 * false, the CAWPE ensemble model will be used instead.
	 */
	private final boolean useHIVECOTEEnsemble;

	/**
	 * Indicator whether the optimized minimum distance search should be used.
	 */
	private boolean useOptimizedMinimumDistSearch = true;

	/**
	 * Constructs a training algorithm for the {@link ShapeletTransformTSClassifier}
	 * classifier specified by the given parameters.
	 * 
	 * @param k
	 *            Number of shapelets extracted in the shapelet search
	 * @param noClusters
	 *            Number of clusters if shapelet clustering is used, i. e. the
	 *            number of retained shapelets
	 * @param qualityMeasure
	 *            Quality measure used to assess the shapelets
	 * @param seed
	 *            Seed used for randomized operations
	 * @param clusterShapelets
	 *            Indicator whether shapelet clustering should be used
	 */
	public ShapeletTransformAlgorithm(final int k, final int noClusters, final IQualityMeasure qualityMeasure,
			final int seed, final boolean clusterShapelets) {
		this.k = k;
		this.qualityMeasure = qualityMeasure;
		this.seed = seed;
		this.noClusters = noClusters;
		this.clusterShapelets = clusterShapelets;
		this.estimateShapeletLengthBorders = true;
		this.useHIVECOTEEnsemble = true;
	}

	/**
	 * Constructs a training algorithm for the {@link ShapeletTransformTSClassifier}
	 * classifier specified by the given parameters.
	 * 
	 * @param k
	 *            Number of shapelets extracted in the shapelet search
	 * @param noClusters
	 *            Number of clusters if shapelet clustering is used, i. e. the
	 *            number of retained shapelets
	 * @param qualityMeasure
	 *            Quality measure used to assess the shapelets
	 * @param seed
	 *            Seed used for randomized operations
	 * @param clusterShapelets
	 *            Indicator whether shapelet clustering should be used
	 * @param minShapeletLength
	 *            The minimal length of the shapelets
	 * @param maxShapeletLength
	 *            The maximal length of the shapelets
	 * @param useHIVECOTEEnsemble
	 *            Indicator whether the HIVE COTE ensemble should be used (CAWPE
	 *            otherwise)
	 * @param timeout
	 *            The timeout used for the training
	 */
	public ShapeletTransformAlgorithm(final int k, final int noClusters, final IQualityMeasure qualityMeasure,
			final int seed, final boolean clusterShapelets, final int minShapeletLength, final int maxShapeletLength,
			final boolean useHIVECOTEEnsemble, final TimeOut timeout) {
		this.k = k;
		this.qualityMeasure = qualityMeasure;
		this.seed = seed;
		this.noClusters = noClusters;
		this.clusterShapelets = clusterShapelets;
		this.estimateShapeletLengthBorders = false;
		this.minShapeletLength = minShapeletLength;
		this.maxShapeletLength = maxShapeletLength;
		this.useHIVECOTEEnsemble = useHIVECOTEEnsemble;
		this.timeout = timeout;
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

		long beginTime = System.currentTimeMillis();

		// Extract time series data and the corresponding targets
		TimeSeriesDataset data = this.getInput();
		if (data == null || data.isEmpty())
			throw new IllegalStateException("The time series input data must not be null or empty!");
		if (data.isMultivariate())
			throw new UnsupportedOperationException("Multivariate datasets are not supported.");

		final double[][] dataMatrix = data.getValuesOrNull(0);
		if (dataMatrix == null)
			throw new IllegalArgumentException(
					"Value matrix must be a valid 2D matrix containing the time series values for all instances!");

		final int[] targetMatrix = data.getTargets();

		try {
			// Estimate min and max
			if (this.estimateShapeletLengthBorders) {
				LOGGER.debug("Starting min max estimation.");
				int[] minMax = estimateMinMax(dataMatrix, targetMatrix, beginTime);
				this.minShapeletLength = minMax[0];
				this.maxShapeletLength = minMax[1];
				LOGGER.debug("Finished min max estimation. min={}, max={}", this.minShapeletLength,
						this.maxShapeletLength);
			}

			// Determine shapelets
			LOGGER.debug("Starting cached shapelet selection with min={}, max={} and k={}...", this.minShapeletLength,
					this.maxShapeletLength, this.k);
			List<Shapelet> shapelets = null;

			shapelets = shapeletCachedSelection(dataMatrix, this.minShapeletLength, this.maxShapeletLength, this.k,
					targetMatrix, beginTime);
			LOGGER.debug("Finished cached shapelet selection. Extracted {} shapelets.", shapelets.size());

			// Cluster shapelets
			if (this.clusterShapelets) {
				LOGGER.debug("Starting shapelet clustering...");
				shapelets = clusterShapelets(shapelets, this.noClusters, beginTime);
				LOGGER.debug("Finished shapelet clustering. Staying with {} shapelets.", shapelets.size());
			}
			this.model.setShapelets(shapelets);

			// Transforming the data using the extracted shapelets
			LOGGER.debug("Transforming the training data using the extracted shapelets.");
			TimeSeriesDataset transfTrainingData = shapeletTransform(data, this.model.getShapelets(), this.timeout,
					beginTime, this.useOptimizedMinimumDistSearch);
			LOGGER.debug("Finished transforming the training data.");

			// Inititalize Weka ensemble
			LOGGER.debug("Initializing ensemble classifier...");
			Classifier classifier = null;
			try {
				classifier = this.useHIVECOTEEnsemble ? initHIVECOTEEnsembleModel() : initCAWPEEnsembleModel();
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

			this.model.setClassifier(classifier);
		} catch (InterruptedException e1) {
			LOGGER.warn("Timeout in training Shapelet Transform classifier. Aborting...");
			throw new AlgorithmException("Could not finish training due to timeout.");
		}

		return this.model;
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
	private int[] estimateMinMax(final double[][] data, final int[] classes, final long beginTime)
			throws InterruptedException {
		int[] result = new int[2];

		long numInstances = data.length;

		List<Shapelet> shapelets = new ArrayList<>();
		for (int i = 0; i < MIN_MAX_ESTIMATION_SAMPLES; i++) {
			double[][] tmpMatrix = new double[MIN_MAX_ESTIMATION_SAMPLES][data[0].length];
			Random rand = new Random(this.seed);
			int[] tmpClasses = new int[MIN_MAX_ESTIMATION_SAMPLES];
			for (int j = 0; j < MIN_MAX_ESTIMATION_SAMPLES; j++) {
				int nextIndex = (int) (rand.nextInt() % numInstances);
				if (nextIndex < 0)
					nextIndex += numInstances;
				for (int k = 0; k < data[0].length; k++)
					tmpMatrix[j][k] = data[nextIndex][k];
				tmpClasses[j] = classes[nextIndex];
			}

			shapelets.addAll(shapeletCachedSelection(tmpMatrix, 3, (int) data[0].length, 10, tmpClasses, beginTime));
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
	public List<Shapelet> clusterShapelets(final List<Shapelet> shapelets, final int noClusters, final long beginTime)
			throws InterruptedException {
		final List<List<Shapelet>> C = new ArrayList<>();
		for (final Shapelet shapelet : shapelets) {
			List<Shapelet> list = new ArrayList<>();
			list.add(shapelet);
			C.add(list);
		}

		// Get clusters
		while (C.size() > noClusters) {
			if ((System.currentTimeMillis() - beginTime) > this.timeout.milliseconds())
				throw new InterruptedException("Interrupted training due to timeout.");

			INDArray M = Nd4j.create(C.size(), C.size());
			for (int i = 0; i < C.size(); i++) {
				for (int j = 0; j < C.size(); j++) {
					double distance = 0;
					int comparisons = C.get(i).size() * C.get(j).size();
					for (int l = 0; l < C.get(i).size(); l++) {
						for (int k = 0; k < C.get(j).size(); k++) {
							Shapelet c_l = C.get(i).get(l);
							Shapelet c_k = C.get(j).get(k);

							if (c_l.getLength() > c_k.getLength())
								distance += this.useOptimizedMinimumDistSearch
										? getMinimumDistanceAmongAllSubsequencesOptimized(c_k, c_l.getData())
										: getMinimumDistanceAmongAllSubsequences(c_k, c_l.getData());
							else
								distance += this.useOptimizedMinimumDistSearch
										? getMinimumDistanceAmongAllSubsequencesOptimized(c_l, c_k.getData())
										: getMinimumDistanceAmongAllSubsequences(c_l, c_k.getData());
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
	 * on the quality measure {@link ShapeletTransformAlgorithm#qualityMeasure}.
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
	private List<Shapelet> shapeletCachedSelection(final double[][] data, final int min, final int max, final int k,
			final int[] classes, final long beginTime) throws InterruptedException {
		List<Map.Entry<Shapelet, Double>> kShapelets = new ArrayList<>();

		final int numInstances = data.length;

		for (int i = 0; i < numInstances; i++) {

			if ((System.currentTimeMillis() - beginTime) > this.timeout.milliseconds())
				throw new InterruptedException("Interrupted training due to timeout.");

			List<Map.Entry<Shapelet, Double>> shapelets = new ArrayList<>();
			for (int l = min; l < max; l++) {
				Set<Shapelet> W_il = generateCandidates(data[i], l, i);
				for (Shapelet s : W_il) {
					List<Double> D_s = findDistances(s, data, this.useOptimizedMinimumDistSearch);
					double quality = qualityMeasure.assessQuality(D_s, classes);
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
	public static List<Map.Entry<Shapelet, Double>> merge(final int k, List<Map.Entry<Shapelet, Double>> kShapelets,
			final List<Map.Entry<Shapelet, Double>> shapelets) {

		kShapelets.addAll(shapelets);

		// Retain only k
		sortByQualityDesc(kShapelets);
		int numRemoveItems = kShapelets.size() - k;
		for (int i = 0; i < numRemoveItems; i++)
			kShapelets.remove(kShapelets.size() - 1);

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
	 * {@link ShapeletTransformAlgorithm#isSelfSimilar(Shapelet, Shapelet)}.
	 * 
	 * @param shapelets
	 *            Shapelets to be compared
	 * @return A sublist from the given <code>shapelets</code> list which does not
	 *         contain any self-similar shapelets
	 */
	public static List<Map.Entry<Shapelet, Double>> removeSelfSimilar(
			final List<Map.Entry<Shapelet, Double>> shapelets) {
		List<Map.Entry<Shapelet, Double>> result = new ArrayList<>();
		for (final Map.Entry<Shapelet, Double> entry : shapelets) {
			// Check whether there is already a self similar shapelet in the result list
			boolean selfSimilarExisting = false;
			for (final Map.Entry<Shapelet, Double> s : result) {
				if (isSelfSimilar(entry.getKey(), s.getKey()))
					selfSimilarExisting = true;
			}

			if (!selfSimilarExisting)
				result.add(entry);
		}

		return result;
	}

	/**
	 * Function checking whether the two given shapelets are self-similar, i. e. if
	 * their indices overlap.
	 * 
	 * @param s1
	 *            First shapelet to be compared
	 * @param s2
	 *            Second shapelet to be compared
	 * @return Returns whether the indices of the given shapelets overlap
	 */
	// Assumes that both shapelets are from the same time series
	private static boolean isSelfSimilar(final Shapelet s1, final Shapelet s2) {
		if (s1.getInstanceIndex() == s2.getInstanceIndex()) {
			return (s1.getStartIndex() < (s2.getStartIndex() + s2.getLength()))
					&& (s2.getStartIndex() < (s1.getStartIndex() + s1.getLength()));
		} else
			return false;
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
	 * @param useOptimizedMinimumDistSearch
	 *            Indicator whether the optimized early abandon minimum distance
	 *            search should be used
	 * @return Returns the list of all minimum distances of the shapelet and all the
	 *         instances
	 */
	public static List<Double> findDistances(final Shapelet s, final double[][] matrix,
			final boolean useOptimizedMinimumDistSearch) {
		List<Double> result = new ArrayList<>();

		for (int i = 0; i < matrix.length; i++) {
			result.add(useOptimizedMinimumDistSearch ? getMinimumDistanceAmongAllSubsequencesOptimized(s, matrix[i])
					: getMinimumDistanceAmongAllSubsequences(s, matrix[i]));
		}

		return result;
	}

	/**
	 * Optimized function returning the minimum distance among all subsequences of
	 * the given <code>timeSeries</code> to the <code>shapelet</code>'s data. This
	 * function implements the algorithm 2 mentioned in the original paper. It
	 * performs the similarity search with online normalization and early abandon.
	 * 
	 * @param shapelet
	 *            The shapelet to be compared to all subsequences
	 * @param timeSeries
	 *            The time series which subsequences are compared to the shapelet's
	 *            data
	 * @return Return the minimum distance among all subsequences
	 */
	public static double getMinimumDistanceAmongAllSubsequencesOptimized(final Shapelet shapelet,
			final double[] timeSeries) {

		double length = shapelet.getLength();
		int m = timeSeries.length;

		// Order normalized shapelet values
		final double[] S_prime = shapelet.getData();
		final List<Integer> A = TimeSeriesUtil.sortIndexes(S_prime, false); // descending
		final double[] F = TimeSeriesUtil.zNormalize(TimeSeriesUtil.getInterval(timeSeries, 0, shapelet.getLength()),
				USE_BIAS_CORRECTION);

		// Online normalization
		double p = 0;
		double q = 0;
		p = MathUtil.sum(TimeSeriesUtil.getInterval(timeSeries, 0, shapelet.getLength()));
		for (int i = 0; i < length; i++) {
			q += timeSeries[i] * timeSeries[i];
		}

		double b = MathUtil.singleSquaredEuclideanDistance(S_prime, F);

		for (int i = 1; i <= m - length; i++) {

			double t_i = timeSeries[i - 1];
			double t_il = timeSeries[i - 1 + shapelet.getLength()];
			p -= t_i;
			q -= t_i * t_i;
			p += t_il;
			q += t_il * t_il;
			double x_bar = p / length;
			double s = q / (length) - x_bar * x_bar;
			s = s < 0.000000001d ? 0d
					: Math.sqrt((USE_BIAS_CORRECTION ? ((double) length / (double) (length - 1d)) : 1d) * s); //

			int j = 0;
			double d = 0d;

			// Early abandon
			while (j < length && d < b) {
				final double normVal = (s == 0.0 ? 0d : (timeSeries[i + A.get(j)] - x_bar) / s);
				final double diff = S_prime[A.get(j)] - normVal;

				d += diff * diff;
				j++;
			}

			if (j == length && d < b) {
				b = d;
			}
		}

		return b / length;
	}

	/**
	 * Function returning the minimum distance among all subsequences of the given
	 * <code>timeSeries</code> to the <code>shapelet</code>'s data.
	 * 
	 * @param shapelet
	 *            The shapelet to be compared to all subsequences
	 * @param timeSeries
	 *            The time series which subsequences are compared to the shapelet's
	 *            data
	 * @return Return the minimum distance among all subsequences
	 */
	public static double getMinimumDistanceAmongAllSubsequences(final Shapelet shapelet, final double[] timeSeries) {
		final int l = shapelet.getLength();
		final int n = timeSeries.length;

		double min = Double.MAX_VALUE;

		double[] normalizedShapeletData = shapelet.getData();

		// TODO: Reference implementation uses i < n-l => Leads sometimes to a better
		// performance => Check this
		for (int i = 0; i < n - l; i++) {
			double tmpED = MathUtil.singleSquaredEuclideanDistance(normalizedShapeletData,
					TimeSeriesUtil.zNormalize(TimeSeriesUtil.getInterval(timeSeries, i, i + l), USE_BIAS_CORRECTION));
			if (tmpED < min)
				min = tmpED;
		}
		return min / l;
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
	 * Initializes the HIVE COTE ensemble consisting of 7 classifiers using a
	 * majority voting strategy as described in J. Lines, S. Taylor and A. Bagnall,
	 * "HIVE-COTE: The Hierarchical Vote Collective of Transformation-Based
	 * Ensembles for Time Series Classification," 2016 IEEE 16th International
	 * Conference on Data Mining (ICDM), Barcelona, 2016, pp. 1041-1046. doi:
	 * 10.1109/ICDM.2016.0133.
	 * 
	 * @return Returns the initialized (but untrained) HIVE COTE ensemble model.
	 */
	private Classifier initHIVECOTEEnsembleModel() {

		Classifier[] classifier = new Classifier[7];

		Vote voter = new Vote();
		voter.setCombinationRule(new SelectedTag(Vote.MAJORITY_VOTING_RULE, Vote.TAGS_RULES));

		// SMO poly2
		SMO smop = new SMO();
		smop.turnChecksOff();
		smop.setBuildCalibrationModels(true);
		PolyKernel kernel = new PolyKernel();
		kernel.setExponent(2);
		smop.setKernel(kernel);
		smop.setRandomSeed(this.seed);
		classifier[0] = smop;

		// Random Forest
		RandomForest rf = new RandomForest();
		rf.setSeed(this.seed);
		rf.setNumIterations(500);
		classifier[1] = rf;

		// Rotation forest
		RotationForest rotF = new RotationForest();
		rotF.setSeed(this.seed);
		rotF.setNumIterations(100);
		classifier[2] = rotF;

		// NN
		IBk nn = new IBk();
		classifier[3] = nn;

		// Naive Bayes
		NaiveBayes nb = new NaiveBayes();
		classifier[4] = nb;

		// C45
		J48 c45 = new J48();
		classifier[5] = c45;

		// SMO linear
		SMO smol = new SMO();
		smol.turnChecksOff();
		smol.setBuildCalibrationModels(true);
		PolyKernel linearKernel = new PolyKernel();
		linearKernel.setExponent(1);
		smol.setKernel(linearKernel);
		classifier[6] = smol;

		voter.setClassifiers(classifier);
		return voter;
	}

	/**
	 * Initializes the CAWPE ensemble model consisting of five classifiers (SMO,
	 * KNN, J48, Logistic and MLP) using a majority voting strategy. The ensemble
	 * uses Weka classifiers.
	 * 
	 * @return Returns an initialized (but untrained) ensemble model.
	 * @throws Exception
	 *             Thrown when the initialization has failed
	 */
	private Classifier initCAWPEEnsembleModel() throws Exception {

		Classifier[] classifiers = new Classifier[5];

		Vote voter = new Vote();
		voter.setCombinationRule(new SelectedTag(Vote.MAJORITY_VOTING_RULE, Vote.TAGS_RULES));

		SMO smo = new SMO();
		smo.turnChecksOff();
		smo.setBuildCalibrationModels(true);
		PolyKernel kl = new PolyKernel();
		kl.setExponent(1);
		smo.setKernel(kl);
		smo.setRandomSeed(seed);
		classifiers[0] = smo;

		IBk k = new IBk(100);
		k.setCrossValidate(true);
		EuclideanDistance ed = new EuclideanDistance();
		ed.setDontNormalize(true);
		k.getNearestNeighbourSearchAlgorithm().setDistanceFunction(ed);
		classifiers[1] = k;

		classifiers[2] = new J48();

		classifiers[3] = new Logistic();

		classifiers[4] = new MultilayerPerceptron();

		voter.setClassifiers(classifiers);
		return voter;
	}

	/**
	 * Performs a shapelet transform on a complete <code>dataSet</code>. See
	 * {@link ShapeletTransformAlgorithm#shapeletTransform(double[], List)}.
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
	 * @param useOptimizedMinimumDistSearch
	 *            Indicator whether the optimized early abandon minimum distance
	 *            search should be used
	 * @return Returns the transformed data set
	 * @throws InterruptedException
	 *             Thrown if there was a timeout
	 */
	public static TimeSeriesDataset shapeletTransform(final TimeSeriesDataset dataSet, final List<Shapelet> shapelets,
			final TimeOut timeout, final long beginTime, final boolean useOptimizedMinimumDistSearch)
			throws InterruptedException {
		// Since the original paper only works on univariate data, this is assumed to be
		// the case
		if (dataSet.isMultivariate())
			throw new UnsupportedOperationException("Multivariate datasets are not supported yet!");

		double[][] timeSeries = dataSet.getValuesOrNull(0);
		if (timeSeries == null) // || timeSeries.shape.length != 2)
			throw new IllegalArgumentException("Time series matrix must be a valid 2d matrix!");

		double[][] transformedTS = new double[timeSeries.length][];

		for (int i = 0; i < timeSeries.length; i++) {
			if (timeout != null && (System.currentTimeMillis() - beginTime) > timeout.milliseconds())
				throw new InterruptedException("Interrupted training due to timeout.");

			transformedTS[i] = shapeletTransform(timeSeries[i], shapelets, useOptimizedMinimumDistSearch);
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
	 * @param useOptimizedMinimumDistSearch
	 *            Indicator whether the optimized early abandon minimum distance
	 *            search should be used
	 * @return Returns the transformed instance feature vector
	 */
	public static double[] shapeletTransform(final double[] instance, final List<Shapelet> shapelets,
			final boolean useOptimizedMinimumDistSearch) {

		double[] transformedTS = new double[shapelets.size()];

		for (int j = 0; j < shapelets.size(); j++) {
			transformedTS[j] = useOptimizedMinimumDistSearch
					? ShapeletTransformAlgorithm.getMinimumDistanceAmongAllSubsequencesOptimized(shapelets.get(j),
							instance)
					: ShapeletTransformAlgorithm.getMinimumDistanceAmongAllSubsequences(shapelets.get(j), instance);
		}

		return transformedTS;
	}

	/**
	 * Getter for {@link ShapeletTransformAlgorithm#useOptimizedMinimumDistSearch}.
	 * 
	 * @return Returns
	 */
	public boolean isUseOptimizedMinimumDistSearch() {
		return useOptimizedMinimumDistSearch;
	}

	/**
	 * Setter for {@link ShapeletTransformAlgorithm#useOptimizedMinimumDistSearch}.
	 * 
	 * @param useOptimizedMinimumDistSearch
	 *            Value to be set
	 */
	public void setUseOptimizedMinimumDistSearch(final boolean useOptimizedMinimumDistSearch) {
		this.useOptimizedMinimumDistSearch = useOptimizedMinimumDistSearch;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerListener(Object listener) {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNumCPUs(int numberOfCPUs) {
		LOGGER.warn(
				"Multithreading is not supported for LearnShapelets yet. Therefore, the number of CPUs is not considered.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumCPUs() {
		LOGGER.warn(
				"Multithreading is not supported for LearnShapelets yet. Therefore, the number of CPUs is not considered.");
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeout(long timeout, TimeUnit timeUnit) {
		this.timeout = new TimeOut(timeout, timeUnit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeout(TimeOut timeout) {
		this.timeout = timeout;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TimeOut getTimeout() {
		return this.timeout;
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
	public Iterator<AlgorithmEvent> iterator() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlgorithmEvent next() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancel() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAlgorithmConfig getConfig() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}
}
