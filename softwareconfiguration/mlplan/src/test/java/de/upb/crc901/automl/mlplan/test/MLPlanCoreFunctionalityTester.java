package de.upb.crc901.automl.mlplan.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.upb.crc901.automl.AutoMLAlgorithmCoreFunctionalityTester;
import de.upb.crc901.mlplan.core.AbstractMLPlanBuilder;
import de.upb.crc901.mlplan.core.MLPlan;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.IAlgorithm;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MLPlanCoreFunctionalityTester extends AutoMLAlgorithmCoreFunctionalityTester {

	@Override
	public IAlgorithm<Instances, Classifier> getAutoMLAlgorithm(final Instances data) {
		try {
			AbstractMLPlanBuilder builder = AbstractMLPlanBuilder.forWeka().withTinyWekaSearchSpace();
			builder.withNodeEvaluationTimeOut(new TimeOut(10, TimeUnit.SECONDS));
			builder.withCandidateEvaluationTimeOut(new TimeOut(5, TimeUnit.SECONDS));
			builder.withNumCpus(1);
			builder.withTimeOut(new TimeOut(5, TimeUnit.SECONDS));
			MLPlan mlplan = new MLPlan(builder, data);
			mlplan.setRandomSeed(1);
			mlplan.setPortionOfDataForPhase2(0f);
			mlplan.setLoggerName(TESTEDALGORITHM_LOGGERNAME);
			return mlplan;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
