package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.Collection;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

public class RankLoss extends AMultiLabelClassificationMeasure {

	private static final double DEFAULT_TIE_LOSS = 0.0;

	private final double tieLoss;

	public RankLoss() {
		this(DEFAULT_TIE_LOSS);
	}

	/**
	 * Create a Ranking Loss measure instance.
	 *
	 * @param tieLoss The loss [0,1] which is accounted for a tie of the predicted relevant and irrelevant label's relevance score.
	 */
	public RankLoss(final double tieLoss) {
		this.tieLoss = tieLoss;
	}

	private double rankingLoss(final IMultiLabelClassification expected, final IMultiLabelClassification actual) {
		int[] expectedRelevantLabels = expected.getRelevantLabels(0.5);
		int[] expectedIrrelevantLabels = expected.getIrrelevantLabels(0.5);
		double[] labelRelevance = actual.getPrediction();
		double wrongRankingCounter = 0;
		for (int expectedRel : expectedRelevantLabels) {
			for (int expectedIrr : expectedIrrelevantLabels) {
				double scoreRelLabel = labelRelevance[expectedRel];
				double scoreIrrLabel = labelRelevance[expectedIrr];
				if (scoreRelLabel == scoreIrrLabel) {
					wrongRankingCounter += this.tieLoss;
				} else if (scoreRelLabel < scoreIrrLabel) {
					wrongRankingCounter += 1.0;
				}
			}
		}
		return wrongRankingCounter / (expectedRelevantLabels.length + expectedIrrelevantLabels.length);
	}

	@Override
	public double loss(final List<? extends Collection<Object>> expected, final List<? extends IMultiLabelClassification> actual) {
		this.checkConsistency(expected, actual);

		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> this.rankingLoss(expected.get(x), actual.get(x))).average();
		if (res.isPresent()) {
			return res.getAsDouble();
		} else {
			throw new IllegalStateException("The ranking loss could not be averaged across all the instances.");
		}
	}
}
