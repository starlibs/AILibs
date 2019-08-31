package autofe.algorithm.hasco.evaluation.test;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.weka.WekaUtil;
import autofe.util.DataSetUtils;
import autofe.util.EvaluationUtils;
import weka.core.Instances;

public class COCOEvaluationTest {

	private static final Logger logger = LoggerFactory.getLogger(COCOEvaluationTest.class);

	@Test
	public void evaluateTest() throws Exception {
		logger.info("Starting COCO evaluation test...");

		/* load dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.MNIST_ID);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, 42, .01f);

		fail("This is a reminder to say that this test yet does not check anything. Result of the computation is: " + EvaluationUtils.calculateCOCOForBatch(split.get(0)));
	}
}
