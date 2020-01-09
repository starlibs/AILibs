package autofe.algorithm.hasco.test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.Timeout;
import org.junit.Assert;
import org.junit.Test;

import autofe.algorithm.hasco.AutoFEMLTwoPhase;
import autofe.algorithm.hasco.HASCOFeatureEngineeringConfig;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;

public class AutoFEMLTwoPhaseTest {
	@Test
	public void autoFEMLTwoPhaseTest() throws Exception {
		DataSet data = DataSetUtils.getDataSetByID(DataSetUtils.FASHION_MNIST_ID);

		List<DataSet> trainTestSplit = DataSetUtils.getStratifiedSplit(data, new Random(42), .7);

		HASCOFeatureEngineeringConfig config = ConfigFactory.create(HASCOFeatureEngineeringConfig.class);

		Timeout autofeTO = new Timeout(60, TimeUnit.SECONDS);
		Timeout mlplanTO = new Timeout(60, TimeUnit.SECONDS);
		Timeout evalTimeout = new Timeout(60, TimeUnit.SECONDS);

		AutoFEMLTwoPhase autofeml = new AutoFEMLTwoPhase(config, 4, "coco", 0.01, 5, 200, 42, autofeTO, mlplanTO,
				evalTimeout, 5);

		System.out.println("Start building AutoFEMLTwoPhase classifier...");

		autofeml.buildClassifier(trainTestSplit.get(0));

		System.out.println("Selected solution: " + autofeml.getSelectedPipeline());
		System.out.println("Scores (AutoFE / MLPlan): " + autofeml.getInternalAutoFEScore() + " / "
				+ autofeml.getInternalMlPlanScore());
		Assert.assertNotNull(autofeml.getSelectedPipeline());
	}
}
