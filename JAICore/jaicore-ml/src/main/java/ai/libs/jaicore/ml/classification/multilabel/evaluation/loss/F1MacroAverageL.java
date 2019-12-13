package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.IRelevanceOrderedLabelSet;

import ai.libs.jaicore.ml.core.evaluation.loss.F1Measure;

public class F1MacroAverageL extends AThresholdBasedMultiLabelClassificationMeasure {

	public F1MacroAverageL(final double threshold) {
		super(threshold);
	}

	public F1MacroAverageL() {
		super();
	}

	@Override
	public double loss(final List<IRelevanceOrderedLabelSet> actual, final List<IRelevanceOrderedLabelSet> expected) {
		return 1 - this.score(expected, actual);
	}

	@Override
	public double score(final List<IRelevanceOrderedLabelSet> expected, final List<IRelevanceOrderedLabelSet> actual) {
		this.checkConsistency(expected, actual);
		int[][] expectedMatrix = this.transposeMatrix(this.listToThresholdedRelevanceMatrix(expected));
		int[][] actualMatrix = this.transposeMatrix(this.listToThresholdedRelevanceMatrix(actual));

		F1Measure loss = new F1Measure(1);
		OptionalDouble res = IntStream.range(0, expectedMatrix.length).mapToDouble(x -> loss.score(expectedMatrix[x], actualMatrix[x])).average();
		if (!res.isPresent()) {
			throw new IllegalStateException("Could not determine average label-wise f measure.");
		} else {
			return res.getAsDouble();
		}
	}

}
