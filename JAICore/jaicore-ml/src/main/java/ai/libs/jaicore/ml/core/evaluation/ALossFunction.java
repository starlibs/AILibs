package ai.libs.jaicore.ml.core.evaluation;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

public abstract class ALossFunction<O> implements IDeterministicPredictionPerformanceMeasure<O> {

	@Override
	public final double loss(final IPredictionAndGroundTruthTable<O> pairTable) {
		return this.loss(pairTable.getGroundTruthAsList(), pairTable.getPredictionsAsList());
	}
}
