package ai.libs.mlplan.multilabel.mekamlplan;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.mlplan.core.MLPlan;

public class MLPlan4Meka extends MLPlan<IMekaClassifier> {

	public MLPlan4Meka(final MLPlanMekaBuilder builder, final ILabeledDataset<?> data) {
		super(builder, data);
	}

}
