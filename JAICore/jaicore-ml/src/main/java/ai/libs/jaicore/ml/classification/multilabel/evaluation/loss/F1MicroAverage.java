package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

import ai.libs.jaicore.ml.classification.loss.dataset.F1Measure;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;

public class F1MicroAverage extends AMultiLabelClassificationMeasure {

	public F1MicroAverage(final double threshold) {
		super(threshold);
	}

	public F1MicroAverage() {
		super();
	}

	@Override
	public double score(final List<? extends int[]> expected, final List<? extends IMultiLabelClassification> predicted) {
		this.checkConsistency(expected, predicted);
		List<Integer> expectedMatrix = expected.stream().flatMapToInt(Arrays::stream).mapToObj(x -> x).collect(Collectors.toList());
		List<ISingleLabelClassification> predictedMatrix = expected.stream().flatMapToInt(Arrays::stream).mapToObj(x -> new SingleLabelClassification(2, x)).collect(Collectors.toList());
		F1Measure loss = new F1Measure(1);
		return loss.score(expectedMatrix, predictedMatrix);
	}
}
