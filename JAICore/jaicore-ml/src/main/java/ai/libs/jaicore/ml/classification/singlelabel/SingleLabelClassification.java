package ai.libs.jaicore.ml.classification.singlelabel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

import ai.libs.jaicore.basic.ArrayUtil;
import ai.libs.jaicore.ml.core.evaluation.Prediction;

public class SingleLabelClassification extends Prediction implements ISingleLabelClassification {

	private double[] labelProbabilities;

	public SingleLabelClassification(final int predicted) {
		super(predicted);
	}

	public SingleLabelClassification(final Map<Integer, Double> labelProbabilities) {
		super(labelWithHighestProbability(labelProbabilities));
		this.labelProbabilities = new double[labelProbabilities.size()];
		labelProbabilities.entrySet().stream().forEach(x -> this.labelProbabilities[x.getKey()] = x.getValue());
	}

	public SingleLabelClassification(final double[] labelProbabilities) {
		super(ArrayUtil.argMax(labelProbabilities));
		this.labelProbabilities = labelProbabilities;
	}

	@Override
	public int getIntPrediction() {
		return (int) super.getPrediction();
	}

	@Override
	public Integer getPrediction() {
		return this.getIntPrediction();
	}

	@Override
	public Integer getLabelWithHighestProbability() {
		return this.getIntPrediction();
	}

	@Override
	public Map<Integer, Double> getClassDistribution() {
		Map<Integer, Double> distributionMap = new HashMap<>();
		IntStream.range(0, this.labelProbabilities.length).forEach(x -> distributionMap.put(x, this.labelProbabilities[x]));
		return distributionMap;
	}

	@Override
	public double getProbabilityOfLabel(final int label) {
		return this.labelProbabilities[label];
	}

	@Override
	public Map<Integer, Double> getClassConfidence() {
		Map<Integer, Double> confidenceMap = new HashMap<>();
		IntStream.range(0, this.labelProbabilities.length).forEach(x -> confidenceMap.put(x, this.labelProbabilities[x]));
		return confidenceMap;
	}

	private static int labelWithHighestProbability(final Map<Integer, Double> labelProbabilities) {
		Entry<Integer, Double> highestProbEntry = null;
		for (Entry<Integer, Double> entry : labelProbabilities.entrySet()) {
			if (highestProbEntry == null || highestProbEntry.getValue() < entry.getValue()) {
				highestProbEntry = entry;
			}
		}
		if (highestProbEntry == null) {
			throw new IllegalArgumentException("No prediction contained");
		} else {
			return highestProbEntry.getKey();
		}
	}
}
