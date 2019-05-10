package de.upb.crc901.automl.mlplan.bigdata;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.junit.Test;

import de.upb.crc901.mlplan.bigdata.MLPlan4BigFileInput;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class MLPlan4BigFileInputTester {

	@Test
	public void test() throws Exception {
		// MLPlan4BigFileInput mlplan = new MLPlan4BigFileInput(new File("testrsc/openml/41103.arff"));

		String origDataSrcName = "testrsc/openml/41103.arff";

		if (false) {
			Instances data = new Instances(new FileReader(new File(origDataSrcName)));
			data.setClassIndex(data.numAttributes() - 1);
			List<Instances> split = WekaUtil.getStratifiedSplit(data, 0, .7f);
			ArffSaver saver = new ArffSaver();
			saver.setInstances(split.get(0));
			saver.setFile(new File(origDataSrcName + ".train"));
			saver.writeBatch();
			saver.setInstances(split.get(1));
			saver.setFile(new File(origDataSrcName + ".test"));
			saver.writeBatch();
			System.exit(0);
		}

		MLPlan4BigFileInput mlplan = new MLPlan4BigFileInput(new File(origDataSrcName + ".train"));
		mlplan.setLoggerName("testedalgorithm");
		Classifier c = mlplan.call();
		System.out.println(c);

		/* check quality */
		Instances testData = new Instances(new FileReader(new File(origDataSrcName + ".test")));
		testData.setClassIndex(testData.numAttributes() - 1);
		Evaluation eval = new Evaluation(testData);
		eval.evaluateModel(c, testData);
		System.out.println(eval.toSummaryString());

		assertNotNull(c);
	}
}
