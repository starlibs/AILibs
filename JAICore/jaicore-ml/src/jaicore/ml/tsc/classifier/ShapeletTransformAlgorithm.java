package jaicore.ml.tsc.classifier;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.tsc.quality_measures.IQualityMeasure;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.SelectedTag;

public class ShapeletTransformAlgorithm extends
		ATSCAlgorithm<CategoricalAttributeType, CategoricalAttributeValue, TimeSeriesDataset, ShapeletTransformClassifier> {

	// TODO: Maybe move to a separate class?
	static class Shapelet {
		private INDArray data;
		private int startIndex;
		private int length;
		private int instanceIndex;
		private double determinedQuality;

		public Shapelet(final INDArray data, final int startIndex, final int length, final int instanceIndex,
				final double determinedQuality) {
			this.data = data;
			this.startIndex = startIndex;
			this.length = length;
			this.instanceIndex = instanceIndex;
			this.determinedQuality = determinedQuality;
		}

		public Shapelet(final INDArray data, final int startIndex, final int length, final int instanceIndex) {
			this.data = data;
			this.startIndex = startIndex;
			this.length = length;
			this.instanceIndex = instanceIndex;
		}

		public INDArray getData() {
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
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ShapeletTransformAlgorithm.class);

	private final IQualityMeasure qualityMeasure;

	private final int k;
	private final int seed;
	private final int noClusters;

	public ShapeletTransformAlgorithm(final int k, final int noClusters, final IQualityMeasure qualityMeasure,
			final int seed) {
		// TODO Auto-generated constructor stub
		this.k = k;
		this.qualityMeasure = qualityMeasure;
		this.seed = seed;
		this.noClusters = noClusters;
	}

	@Override
	public ShapeletTransformClassifier call() throws Exception {

		// Training
		TimeSeriesDataset data = this.getInput();

		if (data.isMultivariate())
			throw new UnsupportedOperationException("Multivariate datasets are not supported.");

		final INDArray dataMatrix = data.getValuesOrNull(0);
		if (dataMatrix == null || dataMatrix.shape().length != 2)
			throw new IllegalArgumentException(
					"Timestamp matrix must be a valid 2D matrix containing the time series values for all instances!");

		final INDArray targetMatrix = data.getTargets();

		// Estimate min and max
		int[] minMax = estimateMinMax(dataMatrix, targetMatrix);
		int min = minMax[0];
		int max = minMax[1];

		List<Shapelet> shapelets = shapeletCachedSelection(dataMatrix, min, max, this.k, targetMatrix);

		shapelets = clusterShapelets(shapelets, this.noClusters);

		Classifier classifier = initEnsembleModel();

		// TODO: Train classifier ensemble
		// classifier.buildClassifier(data);

		this.model.setShapelets(shapelets);
		return this.model;
	}

	private int[] estimateMinMax(final INDArray data, final INDArray classes) {
		int[] result = new int[2];

		List<Shapelet> shapelets = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			INDArray tmpMatrix = Nd4j.create(10, data.shape()[1]);
			Random rand = new Random(this.seed);
			INDArray tmpClasses = Nd4j.create(10);
			for (int j = 0; j < 10; j++) {
				int nextIndex = (int) (rand.nextInt() % data.shape()[0]);
				tmpMatrix.putRow(j, data.getRow(nextIndex));
				tmpClasses.putScalar(j, classes.getDouble(nextIndex));
			}

			shapelets.addAll(shapeletCachedSelection(tmpMatrix, 3, (int) data.shape()[1], 10, tmpClasses));
		}

		sortByLengthDesc(shapelets);

		// Min
		result[0] = shapelets.get(25).getLength();
		// Max
		result[1] = shapelets.get(75).getLength();

		return result;
	}

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

	private List<Shapelet> shapeletCachedSelection(final INDArray data, final int min, final int max, final int k,
			final INDArray classes) {
		List<Map.Entry<Shapelet, Double>> kShapelets = new ArrayList<>();

		final int numInstances = (int) data.shape()[0];

		for (int i = 0; i < numInstances; i++) {
			List<Map.Entry<Shapelet, Double>> shapelets = new ArrayList<>();
			for (int l = min; l < max; l++) {
				Set<Shapelet> W_il = generateCandidates(data.getRow(i), l, i);
				for (Shapelet s : W_il) {
					List<Double> D_s = findDistances(s, data);
					double quality = qualityMeasure.assessQuality(D_s, classes);
					s.setDeterminedQuality(quality);
					shapelets.add(new AbstractMap.SimpleEntry<>(s, quality));
				}
			}
			sortByQualityDesc(shapelets);
			shapelets = removeSelfSimilar(shapelets);
			kShapelets = merge(k, kShapelets, shapelets);
		}

		return kShapelets.stream().map(entry -> entry.getKey()).collect(Collectors.toList());
	}

	private static List<Map.Entry<Shapelet, Double>> merge(final int k, List<Map.Entry<Shapelet, Double>> kShapelets,
			final List<Map.Entry<Shapelet, Double>> shapelets) {

		kShapelets.addAll(shapelets);

		// Retain only k
		sortByQualityDesc(kShapelets);
		for (int i = k; i < kShapelets.size(); i++)
			kShapelets.remove(i);

		return kShapelets;
	}

	private static void sortByQualityDesc(final List<Map.Entry<Shapelet, Double>> list) {
		list.sort((e1, e2) -> e1.getValue().compareTo(e2.getValue()));
		Collections.sort(list, Collections.reverseOrder());
	}

	private static List<Map.Entry<Shapelet, Double>> removeSelfSimilar(
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

		return shapelets;
	}

	// Assumes that both shapelets are from the same time series
	private static boolean isSelfSimilar(final Shapelet s1, final Shapelet s2) {
		if (s1.getInstanceIndex() == s2.getInstanceIndex()) {
			return (s1.getStartIndex() <= (s2.getStartIndex() + s2.getLength()))
					&& (s2.getStartIndex() <= (s1.getStartIndex() + s1.getLength()));
		} else
			return false;
	}

	private static List<Double> findDistances(final Shapelet s, final INDArray matrix) {
		List<Double> result = new ArrayList<>();

		for (int i = 0; i < matrix.shape()[0]; i++) {
			result.add(getMinimumDistanceAmongAllSubsequences(s, matrix.getRow(i)));
		}

		return result;
	}

	public static double getMinimumDistanceAmongAllSubsequences(final Shapelet shapelet, final INDArray timeSeries) {
		final int l = (int) shapelet.getData().length();
		final int n = (int) timeSeries.length();

		double min = Double.MAX_VALUE;

		for (int i = 0; i < n - l; i++) {
			double tmpED = singleEuclideanDistance(shapelet.getData(), timeSeries.get(NDArrayIndex.interval(i, i + l)));
			if (tmpED < min)
				min = tmpED;
		}
		return min;
	}

	// TODO: Change IDistance interface? Work directly on INDArray as opposed to
	// usage of TimeSeriesAttributes?
	private static double singleEuclideanDistance(final INDArray vector1, final INDArray vector2) {
		if (vector1.length() != vector2.length())
			throw new IllegalArgumentException("The lengths of of both vectors must match!");

		double result = 0;
		for (int i = 0; i < vector1.length(); i++) {
			result += Math.pow(vector1.getDouble(i) - vector2.getDouble(i), 2);
		}
		return result;
	}

	private static Set<Shapelet> generateCandidates(final INDArray data, final int l, final int candidateIndex) {
		Set<Shapelet> result = new HashSet<>();

		for (int i = 0; i < data.shape()[0] - l; i++) {
			result.add(new Shapelet(data.get(NDArrayIndex.interval(i, i + l)), i, l, candidateIndex));
		}
		return result;
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
		classifier[1] = rf;

		// Rotation forest
		// TODO

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

	public static TimeSeriesDataset shapeletTransform(final TimeSeriesDataset dataSet, final List<Shapelet> shapelets) {

		// TODO: Deal with multivariate (assuming univariate for now)
		// TimeSeriesAttributeType tsAttType = (TimeSeriesAttributeType)
		// dataSet.getAttributeTypes().get(0);
		// INDArray timeSeries = dataSet.getMatrixForAttributeType(tsAttType);
		INDArray timeSeries = dataSet.getValuesOrNull(0);
		if (timeSeries == null || timeSeries.shape().length != 2)
			throw new IllegalArgumentException("Time series matrix must be a valid 2d matrix!");

		INDArray transformedTS = Nd4j.create(timeSeries.shape()[0], shapelets.size());

		for (int i = 0; i < timeSeries.shape()[0]; i++) {
			for (int j = 0; j < shapelets.size(); j++) {
				transformedTS.putScalar(new int[] { i, j }, ShapeletTransformAlgorithm
						.getMinimumDistanceAmongAllSubsequences(shapelets.get(j), timeSeries.getRow(i)));
			}
		}

		dataSet.replace(0, transformedTS, dataSet.getTimestampsOrNull(0));
		return dataSet;

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
	public void setTimeout(int timeout, TimeUnit timeUnit) {
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
	public AlgorithmEvent nextWithException() throws Exception {
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

	private static void sortByLengthDesc(final List<Shapelet> shapelets) {
		shapelets.sort((s1, s2) -> Integer.compare(s1.getLength(), s2.getLength()));
		Collections.sort(shapelets, Collections.reverseOrder());
	}
}
