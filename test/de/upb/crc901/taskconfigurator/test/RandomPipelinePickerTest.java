package de.upb.crc901.taskconfigurator.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;

import org.junit.Test;

import de.upb.crc901.mlplan.classifiers.RandomPipelinePicker;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class RandomPipelinePickerTest {

	@Test
	public void test() throws Exception {
		final String dataset = "autowekasets/yeast";
		Instances train = new Instances(new BufferedReader(new FileReader("testrsc/" + dataset + "/train.arff")));
		Instances test = new Instances(new BufferedReader(new FileReader("testrsc/" + dataset + "/test.arff")));
		train.setClassIndex(train.numAttributes() - 1);
		test.setClassIndex(test.numAttributes() - 1);
		RandomPipelinePicker optimizer = new RandomPipelinePicker(new File("testrsc/automl2.testset"), false, 1, 3, 20, new Random(10));
		System.out.println("Invoke training method: ");
		optimizer.buildClassifier(train);
		
		Evaluation eval = new Evaluation(train);
		eval.evaluateModel(optimizer, test);
		System.out.println("E out: " + eval.pctIncorrect());
	}
}
