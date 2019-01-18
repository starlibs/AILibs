package jaicore.ml.tsc.classifier;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.quality_measures.IQualityMeasure;
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

public class ShapeletTransformAlgorithm extends
		ASimplifiedTSCAlgorithm<Integer, ShapeletTransformTSClassifier> {

	// TODO: Maybe move to a separate class?
	static class Shapelet {
		private double[] data;
		private int startIndex;
		private int length;
		private int instanceIndex;
		private double determinedQuality;

		public Shapelet(final double[] data, final int startIndex, final int length, final int instanceIndex,
				final double determinedQuality) {
			if (data != null)
				this.data = zNormalize(data, USE_BIAS_CORRECTION);
			this.startIndex = startIndex;
			this.length = length;
			this.instanceIndex = instanceIndex;
			this.determinedQuality = determinedQuality;
		}

		public Shapelet(final double[] data, final int startIndex, final int length, final int instanceIndex) {
			if (data != null)
				this.data = zNormalize(data, USE_BIAS_CORRECTION);
			this.startIndex = startIndex;
			this.length = length;
			this.instanceIndex = instanceIndex;
		}

		public double[] getData() {
			return data;
		}

		public int getLength() {
			return length;
		}

		public int getStartIndex() {
			return startIndex;
		}

		public int getInstanceIndex() {
			return instanceIndex;
		}

		public double getDeterminedQuality() {
			return determinedQuality;
		}

		public void setDeterminedQuality(double determinedQuality) {
			this.determinedQuality = determinedQuality;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Shapelet) {
				Shapelet other = (Shapelet) obj;
				if (data == null && other.getData() != null || data != null && other.getData() == null)
					return false;

				return (data == null && other.getData() == null || Arrays.equals(this.data, other.getData()))
						&& length == other.getLength() && determinedQuality == other.determinedQuality
						&& instanceIndex == other.instanceIndex;
			}
			return super.equals(obj);
		}

		@Override
		public String toString() {
			return "Shapelet [data=" + Arrays.toString(data) + ", startIndex=" + startIndex + ", length=" + length
					+ ", instanceIndex=" + instanceIndex + ", determinedQuality=" + determinedQuality + "]";
		}

	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ShapeletTransformAlgorithm.class);

	private final IQualityMeasure qualityMeasure;

	private static final int MIN_MAX_ESTIMATION_SAMPLES = 10;

	private int k;
	private final int seed;
	private final int noClusters;
	private boolean clusterShapelets;
	private boolean estimateShapeletLengthBorders;

	private int minShapeletLength;
	private int maxShapeletLength;

	private static boolean USE_BIAS_CORRECTION = false;

	public ShapeletTransformAlgorithm(final int k, final int noClusters, final IQualityMeasure qualityMeasure,
			final int seed, final boolean clusterShapelets) {
		this.k = k;
		this.qualityMeasure = qualityMeasure;
		this.seed = seed;
		this.noClusters = noClusters;
		this.clusterShapelets = clusterShapelets;
		this.estimateShapeletLengthBorders = true;
	}

	public ShapeletTransformAlgorithm(final int k, final int noClusters, final IQualityMeasure qualityMeasure,
			final int seed, final boolean clusterShapelets, final int minShapeletLength, final int maxShapeletLength) {
		this.k = k;
		this.qualityMeasure = qualityMeasure;
		this.seed = seed;
		this.noClusters = noClusters;
		this.clusterShapelets = clusterShapelets;
		this.estimateShapeletLengthBorders = false;
		this.minShapeletLength = minShapeletLength;
		this.maxShapeletLength = maxShapeletLength;
	}

	// Training procedure
	@Override
	public ShapeletTransformTSClassifier call() throws AlgorithmException {

		// Extract time series data and the corresponding targets
		TimeSeriesDataset data = this.getInput();
		if (data == null)
			throw new IllegalStateException("The time series input data must not be null!");
		if (data.isMultivariate())
			throw new UnsupportedOperationException("Multivariate datasets are not supported.");

		final double[][] dataMatrix = data.getValuesOrNull(0);
		if (dataMatrix == null)// || dataMatrix.shape().length != 2)
			throw new IllegalArgumentException(
					"Value matrix must be a valid 2D matrix containing the time series values for all instances!");

		final int[] targetMatrix = data.getTargets();

		// Estimate min and max
		if (this.estimateShapeletLengthBorders) {
			LOGGER.debug("Starting min max estimation.");
			int[] minMax = estimateMinMax(dataMatrix, targetMatrix);
			this.minShapeletLength = minMax[0];
			this.maxShapeletLength = minMax[1];
			LOGGER.debug("Finished min max estimation. min={}, max={}", this.minShapeletLength, this.maxShapeletLength);
		}

		// Determine shapelets
		LOGGER.debug("Starting cached shapelet selection with min={}, max={} and k={}...", this.minShapeletLength,
				this.maxShapeletLength, this.k);
		List<Shapelet> shapelets = shapeletCachedSelection(dataMatrix, this.minShapeletLength, this.maxShapeletLength,
				this.k, targetMatrix);
		LOGGER.debug("Finished cached shapelet selection. Extracted {} shapelets.", shapelets.size());

		// Cluster shapelets
		if (this.clusterShapelets) {
			LOGGER.debug("Starting shapelet clustering...");
			shapelets = clusterShapelets(shapelets, this.noClusters);
			LOGGER.debug("Finished shapelet clustering. Staying with {} shapelets.", shapelets.size());
		}
		this.model.setShapelets(shapelets);

		// Transforming the data using the extracted shapelets
		LOGGER.debug("Transforming the training data using the extracted shapelets.");
		TimeSeriesDataset transfTrainingData = shapeletTransform(data, this.model.getShapelets());
		LOGGER.debug("Finished transforming the training data.");

		// Inititalize Weka ensemble
		LOGGER.debug("Initializing ensemble classifier...");
		Classifier classifier = initEnsembleModel();
		// Classifier classifier;
		// try {
		// classifier = initCAWPEEnsembleModel();
		// } catch (Exception e1) {
		// throw new AlgorithmException(e1, "Could not train model due to ensemble
		// exception.");
		// }
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
	 * @return Returns an int[] object of length 2 storing the min (index 0) and the
	 *         max (index 1) estimation
	 */
	private int[] estimateMinMax(final double[][] data, final int[] classes) {
		int[] result = new int[2];

		long numInstances = data.length;

		List<Shapelet> shapelets = new ArrayList<>();
		for (int i = 0; i < MIN_MAX_ESTIMATION_SAMPLES; i++) {
			double[][] tmpMatrix = new double[MIN_MAX_ESTIMATION_SAMPLES][data[0].length];
			// INDArray tmpMatrix = Nd4j.create(MIN_MAX_ESTIMATION_SAMPLES, data[0].length);
			Random rand = new Random(this.seed);
			int[] tmpClasses = new int[MIN_MAX_ESTIMATION_SAMPLES];
			// INDArray tmpClasses = Nd4j.create(MIN_MAX_ESTIMATION_SAMPLES);
			for (int j = 0; j < MIN_MAX_ESTIMATION_SAMPLES; j++) {
				int nextIndex = (int) (rand.nextInt() % numInstances);
				if (nextIndex < 0)
					nextIndex += numInstances;
				for (int k = 0; k < data[0].length; k++)
					tmpMatrix[j][k] = data[nextIndex][k];
				tmpClasses[j] = classes[nextIndex];
			}

			shapelets.addAll(shapeletCachedSelection(tmpMatrix, 3, (int) data[0].length, 10, tmpClasses));
		}

		sortByLengthAsc(shapelets);

		LOGGER.debug("Number of shapelets found in min/max estimation: {}", shapelets.size());

		// Min
		result[0] = shapelets.get(25).getLength();
		// Max
		result[1] = shapelets.get(75).getLength();

		return result;
	}

	// TODO: Update to new dataset representation
	public List<Shapelet> clusterShapelets(final List<Shapelet> shapelets, final int noClusters) {
		final List<List<Shapelet>> C = new ArrayList<>();
		for (final Shapelet shapelet : shapelets) {
			List<Shapelet> list = new ArrayList<>();
			list.add(shapelet);
			C.add(list);
		}

		while (C.size() > noClusters) {
			INDArray M = Nd4j.create(C.size(), C.size());
			for (int i = 0; i < C.size(); i++) {
				for (int j = 0; j < C.size(); j++) {
					double distance = 0;
					int comparisons = C.get(i).size() * C.get(j).size();
					for (int l = 0; l < C.get(i).size(); l++) {
						for (int k = 0; k < C.get(j).size(); k++) {
							Shapelet c_l = C.get(i).get(l);
							Shapelet c_k = C.get(j).get(k);

							if (c_l.length > c_k.length)
								distance += getMinimumDistanceAmongAllSubsequences(c_k, c_l.getData());
							else
								distance += getMinimumDistanceAmongAllSubsequences(c_l, c_k.getData());
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
			Shapelet maxClusterShapelet = getHighestQualityShapeletInList(C_prime);
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

	private static Shapelet getHighestQualityShapeletInList(final List<Shapelet> shapelets) {
		return Collections.max(shapelets,
				(s1, s2) -> (-1) * Double.compare(s1.getDeterminedQuality(), s2.getDeterminedQuality()));
	}

	private List<Shapelet> shapeletCachedSelection(final double[][] data, final int min, final int max, final int k,
			final int[] classes) {
		List<Map.Entry<Shapelet, Double>> kShapelets = new ArrayList<>();

		final int numInstances = data.length;

		for (int i = 0; i < numInstances; i++) {

			List<Map.Entry<Shapelet, Double>> shapelets = new ArrayList<>();
			for (int l = min; l < max; l++) {
				Set<Shapelet> W_il = generateCandidates(data[i], l, i);
				for (Shapelet s : W_il) {
					List<Double> D_s = findDistances(s, data);
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

	private static void sortByQualityDesc(final List<Map.Entry<Shapelet, Double>> list) {
		list.sort((e1, e2) -> (-1) * e1.getValue().compareTo(e2.getValue()));
	}

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

	// Assumes that both shapelets are from the same time series
	private static boolean isSelfSimilar(final Shapelet s1, final Shapelet s2) {
		if (s1.getInstanceIndex() == s2.getInstanceIndex()) {
			return (s1.getStartIndex() < (s2.getStartIndex() + s2.getLength()))
					&& (s2.getStartIndex() < (s1.getStartIndex() + s1.getLength()));
		} else
			return false;
	}

	public static List<Double> findDistances(final Shapelet s, final double[][] matrix) {
		List<Double> result = new ArrayList<>();

		for (int i = 0; i < matrix.length; i++) {
			result.add(getMinimumDistanceAmongAllSubsequences(s, matrix[i]));
		}

		return result;
	}

	// Algorithm 2: Similarity search with online normalization and reordered early
	// abandon
	// TODO: Test
	public static double getMinimumDistanceAmongAllSubsequencesOptimized(final Shapelet shapelet,
			final double[] timeSeries) {

		double length = shapelet.getLength();
		int m = timeSeries.length;

		// final INDArray S = shapelet.getData();
		final double[] S_prime = shapelet.getData();
		final List<Integer> A = sortIndexes(S_prime, false); // descending
		final double[] F = zNormalize(getInterval(timeSeries, 0, shapelet.getLength()), USE_BIAS_CORRECTION);

		double p = 0;
		double q = 0;

		// TODO: Update variables here too

		p = sum(getInterval(timeSeries, 0, shapelet.getLength()));
		for (int i = 0; i < length; i++) {
			q += timeSeries[i] * timeSeries[i];
		}

		double b = singleSquaredEuclideanDistance(S_prime, F);

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
					: Math.sqrt(USE_BIAS_CORRECTION ? ((double) length / (double) (length - 1d)) : 1d * s); //

			int j = 0;
			double d = 0d;


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

	// Analogous to argsort function of ArrayUtil in Nd4j
	public static List<Integer> sortIndexes(final double[] vector, final boolean ascending) {
		List<Integer> result = new ArrayList<>();

		Integer[] indexes = new Integer[(int) vector.length];
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = i;
		}

		Arrays.sort(indexes, new Comparator<Integer>() {
			@Override
			public int compare(final Integer i1, final Integer i2) {
				return (ascending ? 1 : -1) * Double.compare(Math.abs(vector[i1]), Math.abs(vector[i2]));
			}
		});

		for (int i = 0; i < indexes.length; i++) {
			result.add(indexes[i]);
		}

		return result;
	}

	public static double getMinimumDistanceAmongAllSubsequences(final Shapelet shapelet, final double[] timeSeries) {
		final int l = shapelet.getLength();
		final int n = timeSeries.length;

		double min = Double.MAX_VALUE;

		double[] normalizedShapeletData = shapelet.getData();

		// TODO: Reference implementation uses i < n-l => Leads sometimes to a better
		// performance => Check this
		for (int i = 0; i < n - l; i++) {
			double tmpED = singleSquaredEuclideanDistance(normalizedShapeletData,
					zNormalize(getInterval(timeSeries, i, i + l), USE_BIAS_CORRECTION));
			if (tmpED < min)
				min = tmpED;
		}
		return min / l;
	}

	// TODO: Change IDistance interface? Work directly on INDArray as opposed to
	// usage of TimeSeriesAttributes?
	public static double singleSquaredEuclideanDistance(final double[] vector1, final double[] vector2) {
		if (vector1.length != vector2.length)
			throw new IllegalArgumentException("The lengths of of both vectors must match!");

		double distance = 0;
		for (int i = 0; i < vector1.length; i++) {
			distance += Math.pow(vector1[i] - vector2[i], 2);
		}

		return distance;
	}

	// TODO: Use Helens implementation
	public static double[] zNormalize(final double[] dataVector, final boolean besselsCorrection) {
		// TODO: Parameter checks...

		int n = dataVector.length - (besselsCorrection ? 1 : 0);

		double mean = 0; // dataVector.meanNumber().doubleValue();
		for (int i = 0; i < dataVector.length; i++) {
			mean += dataVector[i];
		}
		mean /= dataVector.length;

		// Use Bessel's correction to get the sample stddev
		double stddev = 0; // dataVector.stdNumber(true).doubleValue();
		for (int i = 0; i < dataVector.length; i++) {
			stddev += Math.pow(dataVector[i] - mean, 2);
		}
		stddev /= n;
		stddev = Math.sqrt(stddev);

		double[] result = new double[dataVector.length];
		if (stddev == 0.0)
			return result;

		for (int i = 0; i < result.length; i++) {
			result[i] = (dataVector[i] - mean) / stddev;
		}

		return result;
	}

	public static Set<Shapelet> generateCandidates(final double[] data, final int l, final int candidateIndex) {
		Set<Shapelet> result = new HashSet<>();

		for (int i = 0; i < data.length - l + 1; i++) {
			double[] tmpData = getInterval(data, i, i + l);

			result.add(new Shapelet(tmpData, i, l, candidateIndex));
		}
		return result;
	}

	// end exclusive
	private static double[] getInterval(double[] timeSeries, int start, int end) {
		final double[] result = new double[end - start];
		for (int j = 0; j < end - start; j++) {
			result[j] = timeSeries[j + start];
		}
		return result;
	}

	private static double sum(double[] array) {
		return DoubleStream.of(array).sum();
	}

	// Using HIVE COTE paper ensemble
	private Classifier initEnsembleModel() {

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

	// Using CAWPE ensemble
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

	public static TimeSeriesDataset shapeletTransform(final TimeSeriesDataset dataSet, final List<Shapelet> shapelets) {

		// TODO: Deal with multivariate (assuming univariate for now)
		if (dataSet.isMultivariate())
			throw new UnsupportedOperationException("Multivariate datasets are not supported yet!");

		double[][] timeSeries = dataSet.getValuesOrNull(0);
		if (timeSeries == null) // || timeSeries.shape.length != 2)
			throw new IllegalArgumentException("Time series matrix must be a valid 2d matrix!");

		double[][] transformedTS = new double[timeSeries.length][shapelets.size()];

		for (int i = 0; i < timeSeries.length; i++) {
			for (int j = 0; j < shapelets.size(); j++) {
				transformedTS[i][j] = ShapeletTransformAlgorithm
						.getMinimumDistanceAmongAllSubsequences(shapelets.get(j), timeSeries[i]);
				// transformedTS.putScalar(new int[] { i, j }, ShapeletTransformAlgorithm
				// .getMinimumDistanceAmongAllSubsequences(shapelets.get(j),
				// timeSeries.getRow(i)));
			}
		}

		dataSet.replace(0, transformedTS, dataSet.getTimestampsOrNull(0));
		return dataSet;

	}

	public static double[] shapeletTransform(final double[] instance, final List<Shapelet> shapelets) {

		double[] transformedTS = new double[shapelets.size()];

			for (int j = 0; j < shapelets.size(); j++) {
			transformedTS[j] = ShapeletTransformAlgorithm
					.getMinimumDistanceAmongAllSubsequences(shapelets.get(j), instance);
			}

		return transformedTS;

	}

	@Override
	public void registerListener(Object listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumCPUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimeout(long timeout, TimeUnit timeUnit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimeout(TimeOut timeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public TimeOut getTimeout() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlgorithmEvent nextWithException() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AlgorithmEvent next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	private static void sortByLengthAsc(final List<Shapelet> shapelets) {
		shapelets.sort((s1, s2) -> Integer.compare(s1.getLength(), s2.getLength()));
	}

	@Override
	public IAlgorithmConfig getConfig() {
		// TODO Auto-generated method stub
		return null;
	}
}
