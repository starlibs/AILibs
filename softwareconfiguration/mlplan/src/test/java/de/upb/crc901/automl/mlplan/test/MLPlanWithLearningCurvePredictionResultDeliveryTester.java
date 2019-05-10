package de.upb.crc901.automl.mlplan.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.upb.crc901.automl.AutoMLAlgorithmResultProductionTester;
import de.upb.crc901.mlplan.core.AbstractMLPlanBuilder;
import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanWekaBuilder;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.SimpleRandomSamplingFactory;
import jaicore.ml.learningcurve.extrapolation.ipl.InversePowerLawExtrapolationMethod;
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
