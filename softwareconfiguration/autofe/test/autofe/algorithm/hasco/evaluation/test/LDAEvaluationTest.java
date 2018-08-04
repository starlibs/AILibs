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
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LDA;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Nystroem;

public class LDAEvaluationTest {
	private static final Logger logger = LoggerFactory.getLogger(LDAEvaluationTest.class);

	// @Test
	public void evaluateTest() throws Exception {
		logger.info("Starting cluster evaluation test...");

		/* load dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.CIFAR10_ID);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> dataSplit = WekaUtil.getStratifiedSplit(data, new Random(42), .05f);
		// logger.info("Calculating intermediates...");
		// List<INDArray> intermediate = new ArrayList<>();
		// for (Instance inst : split.get(0)) {
		// // intermediate.add(DataSetUtils.cifar10InstanceToBitmap(inst));
		// intermediate.add(DataSetUtils.cifar10InstanceToMatrix(inst));
		// }
		// logger.info("Finished intermediate calculations.");

		Instances insts = dataSplit.get(0);
		List<Instances> split = WekaUtil.getStratifiedSplit(insts, new Random(42), .7f);

		long timeStart = System.currentTimeMillis();

		LDA lda = new LDA();
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

	@Test
	public void evaluateKernelLDA() throws Exception {
		logger.info("Starting cluster evaluation test...");

		/* load dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.MNIST_ID);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> dataSplit = WekaUtil.getStratifiedSplit(data, new Random(42), .05f);
		// logger.info("Calculating intermediates...");
		// List<INDArray> intermediate = new ArrayList<>();
		// for (Instance inst : split.get(0)) {
		// // intermediate.add(DataSetUtils.cifar10InstanceToBitmap(inst));
		// intermediate.add(DataSetUtils.cifar10InstanceToMatrix(inst));
		// }
		// logger.info("Finished intermediate calculations.");

		Instances insts = dataSplit.get(0);
		List<Instances> split = WekaUtil.getStratifiedSplit(insts, new Random(42), .7f);
		Instances newInsts = split.get(0);
		Instances evalInsts = split.get(1);

		long timeStart = System.currentTimeMillis();

		Nystroem kernelFilter = new Nystroem();
		kernelFilter.setInputFormat(newInsts);
		// // Initialize kernel? (using data, cache size 250007, gamma 0.01)? =>
		// // Defaults
		//
		kernelFilter.setKernel(new RBFKernel(newInsts, 250007, 0.01));
		// kernelFilter.setKernel(new PolyKernel(newInsts, 250007, 2, false));
		// newInsts = Filter.useFilter(newInsts, kernelFilter);
		// evalInsts = Filter.useFilter(evalInsts, kernelFilter);

		LDA lda = new LDA();
		// FLDA flda = new FLDA();

		lda.buildClassifier(newInsts);

		long timeStartEval = System.currentTimeMillis();

		Evaluation eval = new Evaluation(newInsts);
		eval.evaluateModel(lda, evalInsts);
		logger.debug("LDA pct correct: " + eval.pctCorrect());

		long timeTaken = System.currentTimeMillis() - timeStart;
		long timeTakenEval = System.currentTimeMillis() - timeStartEval;

		logger.debug("LDA took " + (timeTaken / 1000) + " s.");
		logger.debug("LDA eval took " + (timeTakenEval / 1000) + " s.");
	}
}
