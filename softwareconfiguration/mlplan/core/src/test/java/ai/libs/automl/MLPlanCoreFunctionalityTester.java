package ai.libs.automl;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;

import ai.libs.mlplan.core.MLPlanSimpleBuilder;

public class MLPlanCoreFunctionalityTester extends AutoMLAlgorithmCoreFunctionalityTester {

	@Override
	public IAlgorithm getAutoMLAlgorithm(final ILabeledDataset data) {
		return new MLPlanSimpleBuilder().withDataset(data).build();
	}
}
