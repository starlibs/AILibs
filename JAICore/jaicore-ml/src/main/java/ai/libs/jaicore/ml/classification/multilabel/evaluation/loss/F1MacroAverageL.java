package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

import ai.libs.jaicore.basic.ArrayUtil;
import ai.libs.jaicore.ml.classification.loss.dataset.F1Measure;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;

public class F1MacroAverageL extends AMultiLabelClassificationMeasure {

	public F1MacroAverageL(final double threshold) {
		super(threshold);
	}

	public F1MacroAverageL() {
		super();
	}

	@Override
	public double score(final List<? extends int[]> expected, final List<? extends IMultiLabelClassification> predicted) {
		this.checkConsistency(expected, predicted);
		int[][] expectedMatrix = ArrayUtil.transposeMatrix(this.listToMatrix(expected));
		int[][] actualMatrix = ArrayUtil.transposeMatrix(this.listToThresholdedRelevanceMatrix(predicted));

		F1Measure loss = new F1Measure(1);
		OptionalDouble res = IntStream.range(0, expectedMatrix.length).mapToDouble(
				x -> loss.score(Arrays.stream(expectedMatrix[x]).mapToObj(Integer::valueOf).collect(Collectors.toList()), Arrays.stream(actualMatrix[x]).mapToObj(y -> new SingleLabelClassification(2, y)).collect(Collectors.toList())))
				.average();
		if (!res.isPresent()) {
			throw new IllegalStateException("Could not determine average label-wise f measure.");
		} else {
			return res.getAsDouble();
		}
	}

}
