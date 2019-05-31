package ai.libs.automl.mlplan.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ai.libs.automl.AutoMLAlgorithmResultProductionTester;
import ai.libs.jaicore.basic.TimeOut;
import ai.libs.jaicore.basic.algorithm.IAlgorithm;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.SimpleRandomSamplingFactory;
import ai.libs.jaicore.ml.learningcurve.extrapolation.ipl.InversePowerLawExtrapolationMethod;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.MLPlanWekaBuilder;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MLPlanWithLearningCurvePredictionResultDeliveryTester extends AutoMLAlgorithmResultProductionTester {

	@Override
	public IAlgorithm<Instances, Classifier> getAutoMLAlgorithm(final Instances data) {
		try {
			MLPlanWekaBuilder builder = AbstractMLPlanBuilder.forWeka();
			builder.withLearningCurveExtrapolationEvaluation(new int[] { 8, 16, 64, 128 }, new SimpleRandomSamplingFactory<>(), .7, new InversePowerLawExtrapolationMethod());
			builder.withNodeEvaluationTimeOut(new TimeOut(15, TimeUnit.MINUTES));
			builder.withCandidateEvaluationTimeOut(new TimeOut(5, TimeUnit.MINUTES));
			MLPlan mlplan = new MLPlan(builder, data);
			mlplan.setRandomSeed(1);
			mlplan.setPortionOfDataForPhase2(.3f);
			mlplan.setNumCPUs(2);
			return mlplan;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
