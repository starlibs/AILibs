package ai.libs.mlplan.sklearn;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.scikitwrapper.IScikitLearnWrapper;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.sklearn.builder.MLPlanScikitLearnBuilder;

public class MLPlan4ScikitLearn extends MLPlan<IScikitLearnWrapper> {

	public MLPlan4ScikitLearn(final MLPlanScikitLearnBuilder builder, final ILabeledDataset<?> data) {
		super(builder, data);
	}
}
