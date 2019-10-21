package ai.libs.jaicore.ml.core.evaluation;

import org.api4.java.ai.ml.classification.execution.IClassificationPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.loss.ILossFunction;

public abstract class ALossFunction implements ILossFunction {

	@Override
	final public double loss(final IClassificationPredictionAndGroundTruthTable pairTable) {
		return this.loss(pairTable.getGroundTruthAsList(), pairTable.getPredictionsAsList());
	}
}
