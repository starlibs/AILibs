package ai.libs.jaicore.ml.classification.multilabel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;
import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassificationPredictionBatch;

public class MultiLabelClassificationPredictionBatch implements IMultiLabelClassificationPredictionBatch {

	private List<? extends IMultiLabelClassification> batch;

	public MultiLabelClassificationPredictionBatch(final List<? extends IMultiLabelClassification> batch) {
		this.batch = batch;
	}

	public MultiLabelClassificationPredictionBatch(final IMultiLabelClassification[] batch) {
		this(Arrays.asList(batch));
	}

	@Override
	public IMultiLabelClassification get(final int index) {
		return this.batch.get(index);
	}

	@Override
	public int getNumPredictions() {
		return this.batch.size();
	}

	@Override
	public List<? extends IMultiLabelClassification> getPredictions() {
		return this.batch;
	}

	@Override
	public double[][] getPredictionMatrix() {
		double[][] predictionMatrix = new double[this.batch.size()][];
		IntStream.range(0, this.batch.size()).forEach(x -> predictionMatrix[x] = this.batch.get(x).getPrediction());
		return predictionMatrix;
	}

	@Override
	public int[][] getThresholdedPredictionMatrix(final double threshold) {
		int[][] predictionMatrix = new int[this.batch.size()][];
		IntStream.range(0, this.batch.size()).forEach(x -> predictionMatrix[x] = this.batch.get(x).getPrediction(threshold));
		return predictionMatrix;
	}

	@Override
	public int[][] getThresholdedPredictionMatrix(final double[] threshold) {
		int[][] predictionMatrix = new int[this.batch.size()][];
		IntStream.range(0, this.batch.size()).forEach(x -> predictionMatrix[x] = this.batch.get(x).getPrediction(threshold));
		return predictionMatrix;
	}

}
