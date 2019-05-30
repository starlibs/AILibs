package de.upb.crc901.automl.mlplan.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.upb.crc901.automl.AutoMLAlgorithmResultProductionTester;
import de.upb.crc901.mlplan.core.AbstractMLPlanBuilder;
import de.upb.crc901.mlplan.core.MLPlan;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.IAlgorithm;
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
