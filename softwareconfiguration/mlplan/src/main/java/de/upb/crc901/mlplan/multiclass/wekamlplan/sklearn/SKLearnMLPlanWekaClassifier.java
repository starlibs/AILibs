package de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn;

import java.io.IOException;

import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import jaicore.ml.evaluation.IInstancesClassifier;
import weka.core.Instances;

public class SKLearnMLPlanWekaClassifier extends MLPlanWekaClassifier implements IInstancesClassifier {

	public SKLearnMLPlanWekaClassifier(final MLPlanBuilder builder) throws IOException {
		super(builder);
	}

	public SKLearnMLPlanWekaClassifier() throws IOException {
		super(new MLPlanBuilder().withAutoSKLearnConfig());
	}

	@Override
	public double[] classifyInstances(final Instances instances) throws Exception {
		/* If the selected classifier can handle batch classification, use this feature. */
		if (super.getSelectedClassifier() instanceof IInstancesClassifier) {
			return ((IInstancesClassifier) super.getSelectedClassifier()).classifyInstances(instances);
		}

		double[] predictions = new double[instances.size()];
		for (int i = 0; i < instances.size(); i++) {
			predictions[i] = super.getSelectedClassifier().classifyInstance(instances.get(i));
		}
		return predictions;
	}

}
