package ai.libs.mlplan.meka;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.mlplan.core.MLPlan;

public class ML2Plan4Meka extends MLPlan<IMekaClassifier> {

	public ML2Plan4Meka(final ML2PlanMekaBuilder builder, final ILabeledDataset<?> data) {
		super(builder, data);
	}

}
