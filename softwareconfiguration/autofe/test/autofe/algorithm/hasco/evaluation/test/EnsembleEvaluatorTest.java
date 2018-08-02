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

import autofe.util.DataSetUtils;
import jaicore.ml.WekaUtil;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

public class EnsembleEvaluatorTest {
	private static final Logger logger = LoggerFactory.getLogger(EnsembleEvaluatorTest.class);

	// @Test
	public void ensembleEvaluatorTest() throws Exception {
		logger.info("Staring cluster evaluation test...");

		/* load dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.SEGMENT_ID);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .7f);

		Instances insts = split.get(0);

		long timeStart = System.currentTimeMillis();

		ReliefFAttributeEval eval = new ReliefFAttributeEval();
		eval.buildEvaluator(insts);

		long timeStartEval = System.currentTimeMillis();

		double attEvalSum = 0;
		for (int i = 0; i < insts.numAttributes(); i++) {
			attEvalSum += eval.evaluateAttribute(i);
		}
		attEvalSum /= insts.numAttributes();

		long timeTaken = System.currentTimeMillis() - timeStart;
		long timeTakenEval = System.currentTimeMillis() - timeStartEval;

		logger.debug("Value: " + attEvalSum);
		logger.debug("Clustering took " + (timeTaken / 1000) + " s.");
		logger.debug("Clustering eval took " + (timeTakenEval / 1000) + " s.");
	}

	@Test
	public void knnEvaluatorTest() throws Exception {
		logger.info("Staring knn evaluation test...");

		/* load dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.CIFAR10_ID);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .05f);

		Instances insts = split.get(0);
		List<Instances> split2 = WekaUtil.getStratifiedSplit(insts, new Random(42), .7f);

		long timeStart = System.currentTimeMillis();

		IBk knn = new IBk(10);
		knn.buildClassifier(split2.get(0));

		long timeStartEval = System.currentTimeMillis();

		Evaluation eval = new Evaluation(split2.get(0));
		eval.evaluateModel(knn, split2.get(1));
		logger.debug("Pct correct: " + eval.pctCorrect());

		long timeTaken = System.currentTimeMillis() - timeStart;
		long timeTakenEval = System.currentTimeMillis() - timeStartEval;

		logger.debug("KNN took " + (timeTaken / 1000) + " s.");
		logger.debug("KNN eval took " + (timeTakenEval / 1000) + " s.");
	}
}
