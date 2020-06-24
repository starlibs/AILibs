package ai.libs.mlplan.multiclass.sklearn;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;

import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.MLPlan;

public class MLPlan4SKLearn<P extends IPrediction, B extends IPredictionBatch> extends MLPlan<ScikitLearnWrapper<P, B>> {

	public MLPlan4SKLearn(final MLPlanSKLearnBuilder<P, B> builder, final ILabeledDataset<?> data) {
		super(builder, data);
	}

}
