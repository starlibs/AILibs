package jaicore.ml.tsc.classifier;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.TimeSeriesInstance;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.tsc.quality_measures.IQualityMeasure;

public class ShapeletTransformAlgorithm extends
		ATSCAlgorithm<CategoricalAttributeType, CategoricalAttributeValue, TimeSeriesDataset, ShapeletTransformClassifier> {

	// TODO: Maybe move to a separate class?
	static class Shapelet {
		private INDArray data;
		private int startIndex;
		private int length;
		private int instanceIndex;

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
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ShapeletTransformAlgorithm.class);

	private final IQualityMeasure<String> qualityMeasure;

	private final int k;

	public ShapeletTransformAlgorithm(final int k, final IQualityMeasure<String> qualityMeasure) {
		// TODO Auto-generated constructor stub
		this.k = k;
		this.qualityMeasure = qualityMeasure;
	}

	@Override
	public ShapeletTransformClassifier call() throws Exception {
		// TODO Auto-generated method stub

		// Training
		TimeSeriesDataset data = this.getInput();
		INDArray dataMatrix = null; // TODO
		final List<String> classValues = new ArrayList<>();
		for (TimeSeriesInstance instance : data) {
			classValues.add(instance.getTargetValue(String.class).getValue());
		}
		// Set<String> classValues = ((ICategoricalAttributeType)
		// data.getTargetType(String.class)).getDomain();

		int max = 0;
		int min = 0;

		List<Shapelet> shapelets = shapeletCachedSelection(dataMatrix, min, max, this.k, classValues);

		this.model.setShapelets(shapelets);

		return this.model;
	}

	private List<Shapelet> shapeletCachedSelection(final INDArray data, final int min, final int max, final int k,
			final List<String> classValues) {
		List<Map.Entry<Shapelet, Double>> kShapelets = new ArrayList<>();

		final int numInstances = (int) data.shape()[0];

		for (int i = 0; i < numInstances; i++) {
			List<Map.Entry<Shapelet, Double>> shapelets = new ArrayList<>();
			for (int l = min; l < max; l++) {
				Set<Shapelet> W_il = generateCandidates(data.getRow(i), l, i);
				for (Shapelet s : W_il) {
					List<Double> D_s = findDistances(s, data);
					double quality = qualityMeasure.assessQuality(D_s, classValues);
					shapelets.add(new AbstractMap.SimpleEntry<>(s, quality));
				}
			}
			shapelets = sortByQuality(shapelets);
			shapelets = removeSelfSimilar(shapelets);
			kShapelets = merge(k, kShapelets, shapelets);
		}

		return kShapelets.stream().map(entry -> entry.getKey()).collect(Collectors.toList());
	}

	private static List<Map.Entry<Shapelet, Double>> merge(final int k, List<Map.Entry<Shapelet, Double>> kShapelets,
			final List<Map.Entry<Shapelet, Double>> shapelets) {

		kShapelets.addAll(shapelets);

		// Retain only k
		kShapelets = sortByQuality(kShapelets);
		for (int i = k; i < kShapelets.size(); i++)
			kShapelets.remove(i);

		return kShapelets;
	}

	private static List<Map.Entry<Shapelet, Double>> sortByQuality(final List<Map.Entry<Shapelet, Double>> list) {
		list.sort((e1, e2) -> e1.getValue().compareTo(e2.getValue()));
		return list;
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

}
