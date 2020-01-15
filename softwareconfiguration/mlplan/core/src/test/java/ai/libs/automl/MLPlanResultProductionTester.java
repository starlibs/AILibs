package ai.libs.automl;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;

import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.MLPlanSimpleBuilder;

public class MLPlanResultProductionTester extends AutoMLAlgorithmResultProductionTester{

	@Override
	public IAlgorithm<ILabeledDataset<?>, IClassifier> getAutoMLAlgorithm(final ILabeledDataset<?> data) {
		this.logger.info("Creating ML-Plan instance.");
		MLPlan<IClassifier> mlplan = new MLPlanSimpleBuilder().withDataset(data).build();
		this.logger.info("done");
		return mlplan;
	}
}
