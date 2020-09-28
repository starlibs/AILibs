package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

public class TrueNegatives extends ASingleLabelClassificationPerformanceMeasure {

	private final int positiveClass;

	public TrueNegatives(final int positiveClass) {
		this.positiveClass = positiveClass;
	}

	@Override
	public double score(final List<? extends Integer> expected, final List<? extends ISingleLabelClassification> predicted) {
		return IntStream.range(0, expected.size()).filter(i -> !expected.get(i).equals(this.positiveClass) && expected.get(i).equals(predicted.get(i).getPrediction())).count();
	}

}
