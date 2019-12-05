package ai.libs.automl.mlplan.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.TimeOut;

import ai.libs.automl.AutoMLAlgorithmResultProductionTester;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;

public class MLPlanResultDeliveryTester extends AutoMLAlgorithmResultProductionTester {

	@Override
	public IAlgorithm<ILabeledDataset<?>, IWekaClassifier> getAutoMLAlgorithm(final ILabeledDataset<?> data) {
		try {
			MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
			builder.withNodeEvaluationTimeOut(new TimeOut(5, TimeUnit.MINUTES));
			builder.withCandidateEvaluationTimeOut(new TimeOut(1, TimeUnit.MINUTES));
			builder.withNumCpus(8);
			MLPlan<IWekaClassifier> mlplan = builder.withDataset(data).build();
			mlplan.setRandomSeed(1);
			mlplan.setPortionOfDataForPhase2(.0f);
			return mlplan;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
