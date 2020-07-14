package ai.libs.mlplan.multiclass.sklearn;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;

import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.sklearn.builder.MLPlanSKLearnBuilder;

public class MLPlan4SKLearn extends MLPlan<ScikitLearnWrapper<IPrediction, IPredictionBatch>> {

	public MLPlan4SKLearn(final MLPlanSKLearnBuilder builder, final ILabeledDataset<?> data) {
		super(builder, data);
	}
}
