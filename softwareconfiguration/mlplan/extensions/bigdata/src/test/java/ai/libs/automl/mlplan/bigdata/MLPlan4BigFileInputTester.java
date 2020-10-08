package ai.libs.automl.mlplan.bigdata;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.Timeout;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.mlplan.bigdata.MLPlan4BigFileInput;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

@Disabled("This project is currently not maintained")
public class MLPlan4BigFileInputTester extends ATest {

	@Test
	public void test() throws Exception {

		this.logger.info("Reading in data.");
		ILabeledDataset<?> data = new WekaInstances(OpenMLDatasetReader.deserializeDataset(1240));
		this.logger.info("Splitting data.");
		List<ILabeledDataset<?>> split = SplitterUtil.getSimpleTrainTestSplit(data, new Random(0), .7f);
		this.logger.info("Ready. Now saving separate files.");
		String origDataSrcName = "bigdata";
		ArffSaver saver = new ArffSaver();
		saver.setInstances(new WekaInstances(split.get(0)).getInstances());
		saver.setFile(new File(origDataSrcName + ".train"));
		saver.writeBatch();
		saver.setInstances(new WekaInstances(split.get(1)).getInstances());
		saver.setFile(new File(origDataSrcName + ".test"));
		saver.writeBatch();
		this.logger.info("Done. Now running ML-Plan");

		MLPlan4BigFileInput mlplan = new MLPlan4BigFileInput(new File(origDataSrcName + ".train"));
		mlplan.setTimeout(new Timeout(5, TimeUnit.MINUTES));
		mlplan.setLoggerName("testedalgorithm");
		long start = System.currentTimeMillis();
		Classifier c = mlplan.call();
		System.out.println("Observed output: " + c + " after " + (System.currentTimeMillis() - start) + "ms. Now validating the model");

		/* check quality */
		Instances testData = new Instances(new FileReader(new File(origDataSrcName + ".test")));
		testData.setClassIndex(testData.numAttributes() - 1);
		Evaluation eval = new Evaluation(testData);
		eval.evaluateModel(c, testData);
		System.out.println(eval.toSummaryString());

		assertNotNull(c);
	}
}
