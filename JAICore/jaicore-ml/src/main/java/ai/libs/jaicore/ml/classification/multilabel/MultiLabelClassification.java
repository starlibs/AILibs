package ai.libs.jaicore.ml.classification.multilabel;

import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

import ai.libs.jaicore.ml.core.evaluation.Prediction;

public class MultiLabelClassification extends Prediction implements IMultiLabelClassification {

	private static final double DEFAULT_THRESHOLD = 0.5;

	private double[] threshold;

	public MultiLabelClassification(final double[] predicted) {
		this(predicted, DEFAULT_THRESHOLD);
	}

	public MultiLabelClassification(final double[] predicted, final double threshold) {
		this(predicted, IntStream.range(0, predicted.length).mapToDouble(x -> threshold).toArray());
	}

	public MultiLabelClassification(final double[] predicted, final double[] threshold) {
		super(predicted);
		this.threshold = threshold;
	}

	@Override
	public double[] getPrediction() {
		return (double[]) super.getPrediction();
	}

	public int[] getThresholdedPrediction() {
		return IntStream.range(0, this.getPrediction().length).map(x -> this.getPrediction()[x] >= this.threshold[x] ? 1 : 0).toArray();
	}

	@Override
	public int[] getPrediction(final double threshold) {
		return IntStream.range(0, this.getPrediction().length).map(x -> this.getPrediction()[x] >= threshold ? 1 : 0).toArray();
	}

	@Override
	public int[] getPrediction(final double[] threshold) {
		return IntStream.range(0, this.getPrediction().length).map(x -> this.getPrediction()[x] >= threshold[x] ? 1 : 0).toArray();
	}

	@Override
	public int[] getRelevantLabels(final double threshold) {
		return IntStream.range(0, this.getPrediction().length).filter(x -> this.getPrediction()[x] >= threshold).toArray();
	}

	@Override
	public int[] getIrrelevantLabels(final double threshold) {
		return IntStream.range(0, this.getPrediction().length).filter(x -> this.getPrediction()[x] < threshold).toArray();
	}

}
