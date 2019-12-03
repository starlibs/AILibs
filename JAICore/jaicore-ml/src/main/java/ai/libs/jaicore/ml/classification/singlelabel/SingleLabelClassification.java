package ai.libs.jaicore.ml.classification.singlelabel;

import java.util.Map;
import java.util.Map.Entry;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

import ai.libs.jaicore.ml.core.evaluation.Prediction;

public class SingleLabelClassification extends Prediction implements ISingleLabelClassification {

	private Map<Integer, Double> labelProbabilities;

	public SingleLabelClassification(final int predicted) {
		super(predicted);
	}

	public SingleLabelClassification(final Map<Integer, Double> labelProbabilities) {
		super(labelWithHighestProbability(labelProbabilities));
		this.labelProbabilities = labelProbabilities;
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
		return this.labelProbabilities;
	}

	@Override
	public double getProbabilityOfLabel(final int label) {
		return this.labelProbabilities.containsKey(label) ? this.labelProbabilities.get(label) : 0;
	}

	@Override
	public Map<Integer, Double> getClassConfidence() {
		throw new UnsupportedOperationException("Not yet implemented.");
	}
}
