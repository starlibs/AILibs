package autofe.algorithm.hasco.test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import autofe.util.test.DataSetUtilsTest;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;

import autofe.algorithm.hasco.AutoFEMLComplete;
import autofe.algorithm.hasco.AutoFEWekaPipelineFactory;
import autofe.algorithm.hasco.MLPlanFEWekaClassifierConfig;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;

public class AutoFEMLCompleteTest {
	@Test
	public void autoFEMLCompleteTest() throws Exception {

		DataSet data = DataSetUtils.getDataSetByID(DataSetUtils.MNIST_ID);
		long[] shape = DataSetUtilsTest.MNIST_INPUT_SHAPE;

		List<DataSet> trainTestSplit = DataSetUtils.getStratifiedSplit(data, new Random(42), .7);

		MLPlanFEWekaClassifierConfig config = ConfigFactory.create(MLPlanFEWekaClassifierConfig.class);

		AutoFEWekaPipelineFactory factory = new AutoFEWekaPipelineFactory(new FilterPipelineFactory(shape),
				new WEKAPipelineFactory());

		AutoFEMLComplete autofeml = new AutoFEMLComplete(42, 0.01, 5, 200, config, factory);
		autofeml.setTimeoutForNodeEvaluation(15);
		autofeml.setTimeoutForSingleSolutionEvaluation(15);
		autofeml.setTimeout(30, TimeUnit.SECONDS);

		System.out.println("Start building AutoFEML classifier...");

		autofeml.buildClassifier(trainTestSplit.get(0));

		System.out.println("Solution: " + autofeml.getSelectedPipeline());
		System.out.println("Internal score: " + autofeml.getInternalValidationErrorOfSelectedClassifier());
		Assert.assertTrue(autofeml.getInternalValidationErrorOfSelectedClassifier() < 1);
	}
}
