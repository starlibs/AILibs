package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

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
		List<String> expectedRelevantLabels = expected.getPrediction();
		List<String> expectedIrrelevantLabels = expected.getIrrelevantLabels();

		List<String> actualLabelset = actual.getLabelSet();
		double[] labelRelevance = actual.getLabelRelevanceVector();

		double wrongRankingCounter = 0;
		for (String expectedRel : expectedRelevantLabels) {
			for (String expectedIrr : expectedIrrelevantLabels) {
				double scoreRelLabel = labelRelevance[actualLabelset.indexOf(expectedRel)];
				double scoreIrrLabel = labelRelevance[actualLabelset.indexOf(expectedIrr)];
				if (scoreRelLabel == scoreIrrLabel) {
					wrongRankingCounter += this.tieLoss;
				} else if (scoreRelLabel < scoreIrrLabel) {
					wrongRankingCounter += 1.0;
				}
			}
		}
		return wrongRankingCounter / (expectedRelevantLabels.size() + expectedIrrelevantLabels.size());
	}

	@Override
	public double loss(final List<IMultiLabelClassification> expected, final List<IMultiLabelClassification> actual) {
		this.checkConsistency(expected, actual);

		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> this.rankingLoss(expected.get(x), actual.get(x))).average();
		if (res.isPresent()) {
			return res.getAsDouble();
		} else {
			throw new IllegalStateException("The ranking loss could not be averaged across all the instances.");
		}
	}
}
