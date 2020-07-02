package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;
import org.api4.java.ai.ml.classification.multilabel.evaluation.loss.IMultiLabelClassificationPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.classification.loss.dataset.APredictionPerformanceMeasure;

public abstract class AMultiLabelClassificationMeasure extends APredictionPerformanceMeasure<int[], IMultiLabelClassification> implements IMultiLabelClassificationPredictionPerformanceMeasure {

	private static final double DEFAULT_THRESHOLD = 0.5;

	private final double threshold;

	protected AMultiLabelClassificationMeasure(final double threshold) {
		super();
		this.threshold = threshold;
	}

	protected AMultiLabelClassificationMeasure() {
		this(DEFAULT_THRESHOLD);
	}

	public double getThreshold() {
		return this.threshold;
	}

	protected double[][] listToRelevanceMatrix(final List<? extends IMultiLabelClassification> classificationList) {
		double[][] matrix = new double[classificationList.size()][];
		IntStream.range(0, classificationList.size()).forEach(x -> matrix[x] = classificationList.get(x).getPrediction());
		return matrix;
	}

	protected int[][] listToThresholdedRelevanceMatrix(final List<? extends IMultiLabelClassification> classificationList) {
		int[][] matrix = new int[classificationList.size()][];
		IntStream.range(0, classificationList.size()).forEach(x -> matrix[x] = classificationList.get(x).getPrediction(this.threshold));
		return matrix;
	}

	protected Set<Integer> getThresholdedPredictionAsSet(final IMultiLabelClassification prediction) {
		return Arrays.stream(prediction.getThresholdedPrediction()).mapToObj(Integer::valueOf).collect(Collectors.toSet());
	}

	protected int[][] listToMatrix(final List<? extends int[]> classificationList) {
		int[][] matrix = new int[classificationList.size()][];
		IntStream.range(0, classificationList.size()).forEach(x -> matrix[x] = classificationList.get(x));
		return matrix;
	}
}
