package ai.libs.mlplan.multiclass.wekamlplan;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.mlplan.core.MLPlan;

public class MLPlan4Weka extends MLPlan<IWekaClassifier> {

	MLPlan4Weka(final MLPlanWekaBuilder builder, final ILabeledDataset<?> data) {
		super(builder, data);
	}

}
