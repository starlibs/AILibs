package autofe.algorithm.hasco.evaluation.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.util.DataSetUtils;
import autofe.util.EvaluationUtils;
import autofe.util.FileUtils;
import jaicore.ml.WekaUtil;
import weka.core.Instances;

public class COCOEvaluationTest {

	private static final Logger logger = LoggerFactory.getLogger(COCOEvaluationTest.class);

	// @Test
	public void evaluateTest() throws Exception {
		logger.info("Starting COCO evaluation test...");

		/* load dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.MNIST_ID);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .01f);

		logger.info("Result: " + EvaluationUtils.calculateCOCOForBatch(split.get(0)));
	}

	// @Test
	public void evaluateCOCOTest() throws InterruptedException {
		logger.info("Starting COED evaluation test...");

		/* load dataset */
		Instances instances = FileUtils.readSingleInstances("C:\\Users\\Julian\\Desktop\\mnist_7_0.arff");

		logger.info("Result: " + EvaluationUtils.calculateCOCOForBatch(instances));

		Instances instances1 = FileUtils.readSingleInstances("C:\\Users\\Julian\\Desktop\\mnist_7_2.arff");

		logger.info("Result: " + EvaluationUtils.calculateCOCOForBatch(instances1));

		Instances instances2 = FileUtils.readSingleInstances("C:\\Users\\Julian\\Desktop\\mnist_7_3.arff");

		logger.info("Result: " + EvaluationUtils.calculateCOCOForBatch(instances2));
	}
}
