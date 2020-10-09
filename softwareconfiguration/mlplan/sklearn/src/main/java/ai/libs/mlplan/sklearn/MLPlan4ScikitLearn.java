package ai.libs.mlplan.sklearn;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;

import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.sklearn.builder.MLPlanScikitLearnBuilder;

public class MLPlan4ScikitLearn extends MLPlan<ScikitLearnWrapper<IPrediction, IPredictionBatch>> {

	public MLPlan4ScikitLearn(final MLPlanScikitLearnBuilder builder, final ILabeledDataset<?> data) {
		super(builder, data);
	}
}
