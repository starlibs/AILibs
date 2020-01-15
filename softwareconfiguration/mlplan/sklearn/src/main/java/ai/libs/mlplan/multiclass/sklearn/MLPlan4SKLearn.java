package ai.libs.mlplan.multiclass.sklearn;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.MLPlan;

public class MLPlan4SKLearn extends MLPlan<ScikitLearnWrapper> {

	public MLPlan4SKLearn(final MLPlanSKLearnBuilder builder, final ILabeledDataset<?> data) {
		super(builder, data);
	}

}
