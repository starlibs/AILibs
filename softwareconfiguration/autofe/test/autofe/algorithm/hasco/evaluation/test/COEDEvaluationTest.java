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

public class COEDEvaluationTest {

	private static final Logger logger = LoggerFactory.getLogger(COEDEvaluationTest.class);

	// @Test
	public void coedEvalTest() throws Exception {
		logger.info("Starting COED evaluation test...");

		/* load dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.SEGMENT_ID);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .01f);

		logger.info("Result: " + EvaluationUtils.calculateCOEDForBatch(split.get(0)));
	}

	// @Test
	public void coedEval2Test() throws Exception {
		logger.info("Starting COED evaluation test...");

		/* load dataset */
		Instances instances = FileUtils.readSingleInstances("C:\\Users\\Julian\\Desktop\\fashion-mnist_7_0.arff");

		logger.info("Result: " + EvaluationUtils.calculateCOEDForBatch(instances));

		Instances instances1 = FileUtils.readSingleInstances("C:\\Users\\Julian\\Desktop\\fashion-mnist_7_1.arff");

		logger.info("Result: " + EvaluationUtils.calculateCOEDForBatch(instances1));

		Instances instances2 = FileUtils.readSingleInstances("C:\\Users\\Julian\\Desktop\\fashion-mnist_7_2.arff");

		logger.info("Result: " + EvaluationUtils.calculateCOEDForBatch(instances2));
	}
}
