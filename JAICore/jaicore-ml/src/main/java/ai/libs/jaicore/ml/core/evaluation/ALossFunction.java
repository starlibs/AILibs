package ai.libs.jaicore.ml.core.evaluation;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicHomogeneousPredictionPerformanceMeasure;

public abstract class ALossFunction<O> implements IDeterministicHomogeneousPredictionPerformanceMeasure<O> {

	@Override
	public final double loss(final IPredictionAndGroundTruthTable<? extends O, ? extends O> pairTable) {
		return this.loss(pairTable.getGroundTruthAsList(), pairTable.getPredictionsAsList());
	}
}
