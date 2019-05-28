package autofe.algorithm.hasco.test;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import autofe.util.test.DataSetUtilsTest;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;

import autofe.algorithm.hasco.HASCOFeatureEngineering;
import autofe.algorithm.hasco.HASCOFeatureEngineeringConfig;
import autofe.algorithm.hasco.evaluation.COCOObjectEvaluator;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;

public class HASCOFeatureEngineeringTest {
	@Test
	public void hascoImageFETest() throws Exception {

		DataSet data = DataSetUtils.getDataSetByID(DataSetUtils.FASHION_MNIST_ID);
		long[] shape = DataSetUtilsTest.FASHION_MNIST_SHAPE;

		List<DataSet> trainTestSplit = DataSetUtils.getStratifiedSplit(data, new Random(42), .7);

		HASCOFeatureEngineeringConfig config = ConfigFactory.create(HASCOFeatureEngineeringConfig.class);

		HASCOFeatureEngineering hascoImageFE = new HASCOFeatureEngineering(new File("model/catalano/catalano.json"),
				new FilterPipelineFactory(shape), new COCOObjectEvaluator(), config);
		hascoImageFE.setTimeout(60, TimeUnit.SECONDS);
		hascoImageFE.setTimeoutForNodeEvaluation(60);
		hascoImageFE.setTimeoutForSingleSolutionEvaluation(60);

		FilterPipeline pipe = hascoImageFE.build(trainTestSplit.get(0));
		System.out.println(pipe);
		System.out.println("Final score: " + hascoImageFE.getInternalValidationErrorOfSelectedClassifier());
		Assert.assertNotNull(pipe);
	}
}
