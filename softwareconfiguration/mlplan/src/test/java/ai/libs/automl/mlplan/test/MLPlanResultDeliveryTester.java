package ai.libs.automl.mlplan.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ai.libs.automl.AutoMLAlgorithmResultProductionTester;
import ai.libs.jaicore.basic.TimeOut;
import ai.libs.jaicore.basic.algorithm.IAlgorithm;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.MLPlan;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MLPlanResultDeliveryTester extends AutoMLAlgorithmResultProductionTester {

	@Override
	public IAlgorithm<Instances, Classifier> getAutoMLAlgorithm(final Instances data) {
		try {
			AbstractMLPlanBuilder builder = AbstractMLPlanBuilder.forWeka();
			builder.withNodeEvaluationTimeOut(new TimeOut(15, TimeUnit.MINUTES));
			builder.withCandidateEvaluationTimeOut(new TimeOut(5, TimeUnit.MINUTES));
			builder.withNumCpus(4);
			MLPlan mlplan = new MLPlan(builder, data);
			mlplan.setRandomSeed(1);
			mlplan.setPortionOfDataForPhase2(.0f);
			return mlplan;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
