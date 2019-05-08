package de.upb.crc901.automl.mlplan.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.upb.crc901.automl.AutoMLAlgorithmResultProductionTester;
import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanBuilder;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.IDataset;
import weka.classifiers.Classifier;

public class MLPlanResultDeliveryTester extends AutoMLAlgorithmResultProductionTester {

	@Override
	public IAlgorithm<IDataset, Classifier> getAutoMLAlgorithm(final IDataset data) {
		try {
			MLPlanBuilder builder = new MLPlanBuilder().withAutoWEKAConfiguration().withRandomCompletionBasedBestFirstSearch();
			builder.withTimeoutForNodeEvaluation(new TimeOut(15, TimeUnit.MINUTES));
			builder.withTimeoutForSingleSolutionEvaluation(new TimeOut(5, TimeUnit.MINUTES));
			MLPlan mlplan = new MLPlan(builder, data);
			mlplan.setRandomSeed(1);
			mlplan.setPortionOfDataForPhase2(.0f);
			mlplan.setNumCPUs(2);
			return mlplan;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
