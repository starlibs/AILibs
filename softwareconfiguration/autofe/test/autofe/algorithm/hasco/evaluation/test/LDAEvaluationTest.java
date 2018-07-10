package autofe.algorithm.hasco.evaluation.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.test.AutoFETest;
import autofe.util.DataSetUtils;
import jaicore.ml.WekaUtil;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LDA;
import weka.core.Instances;

public class LDAEvaluationTest extends AutoFETest {
	private static final Logger logger = LoggerFactory.getLogger(LDAEvaluationTest.class);

	@Test
	public void evaluateTest() throws Exception {
		logger.info("Staring cluster evaluation test...");

		/* load dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.CIFAR10_ID);
		File file = ds.getDataset(API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .25f);
		// logger.info("Calculating intermediates...");
		// List<INDArray> intermediate = new ArrayList<>();
		// for (Instance inst : split.get(0)) {
		// // intermediate.add(DataSetUtils.cifar10InstanceToBitmap(inst));
		// intermediate.add(DataSetUtils.cifar10InstanceToMatrix(inst));
		// }
		// logger.info("Finished intermediate calculations.");

		Instances insts = split.get(0);

		long timeStart = System.currentTimeMillis();

		// TODO: Perform LDA
		LDA lda = new LDA();
		// FLDA lda = new FLDA();
		lda.buildClassifier(split.get(0));

		long timeStartEval = System.currentTimeMillis();

		Evaluation eval = new Evaluation(split.get(0));
		eval.evaluateModel(lda, split.get(1));
		logger.debug("LDA pct correct: " + eval.pctCorrect());

		long timeTaken = System.currentTimeMillis() - timeStart;
		long timeTakenEval = System.currentTimeMillis() - timeStartEval;

		logger.debug("LDA took " + (timeTaken / 1000) + " s.");
		logger.debug("LDA eval took " + (timeTakenEval / 1000) + " s.");
	}
}
