package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

public class ErrorRate extends ASingleLabelPredictionPerformanceMeasure {

	ErrorRate() {
		/* empty constructor to avoid direct instantiation. Use the enum instead. */
	}

	@Override
	public double loss(final List<? extends Integer> expected, final List<? extends ISingleLabelClassification> predicted) {
		this.checkConsistency(expected, predicted);

		double sumOfZOLoss = 0.0;
		for (int i = 0; i < expected.size(); i++) {
			sumOfZOLoss += expected.get(i).equals(predicted.get(i).getPrediction()) ? 0.0 : 1.0;
		}
		return sumOfZOLoss / expected.size();
	}

}
