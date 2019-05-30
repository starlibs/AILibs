package jaicore.ml.core.evaluation.measure.multilabel;

import jaicore.ml.core.evaluation.measure.LossScoreTransformer;

public class RankScore extends LossScoreTransformer<double[]> implements IMultilabelMeasure {

	private static final RankLoss RANK_LOSS = new RankLoss();

	public RankScore() {
		super(RANK_LOSS);
	}

}
