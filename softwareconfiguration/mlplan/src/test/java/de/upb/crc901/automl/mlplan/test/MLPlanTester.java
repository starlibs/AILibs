package de.upb.crc901.automl.mlplan.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.upb.crc901.automl.AutoMLAlgorithmTester;
import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanBuilder;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.IAlgorithm;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MLPlanTester extends AutoMLAlgorithmTester {

	@Override
	public IAlgorithm<Instances, Classifier> getAutoMLAlgorithm(final Instances data) {
		try {
			MLPlanBuilder builder = new MLPlanBuilder().withTinyTestConfiguration().withRandomCompletionBasedBestFirstSearch();
			builder.withTimeoutForNodeEvaluation(new TimeOut(10, TimeUnit.SECONDS));
			builder.withTimeoutForSingleSolutionEvaluation(new TimeOut(5, TimeUnit.SECONDS));
			MLPlan mlplan = new MLPlan(builder, data);
			mlplan.setRandomSeed(1);
			mlplan.setPortionOfDataForPhase2(0f);
			mlplan.setLoggerName("mlplan");
			mlplan.setTimeout(60, TimeUnit.SECONDS);
			mlplan.setNumCPUs(1);
			return mlplan;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
