package de.upb.crc901.taskconfigurator.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class BaseLearnerTest {
	

	@Test
	public void check() throws Exception {
		Classifier[] portfolio = {
//				new BayesNet(), new NaiveBayes(), 
//				new SimpleLogistic(),
//				new IBk(), new KStar(),
//				new DecisionTable(),
//				new JRip(), new OneR(),
//				new PART(),
//				new ZeroR(), new DecisionStump(), new J48(),
//				new LMT(),
				new RandomForest(),
//				new RandomTree(),
//				new REPTree(),
//				new Logistic(),
//				new MultilayerPerceptron()
				};
		
		Instances data = new Instances(new BufferedReader(new FileReader("testrsc/polychotomous/vowel.arff")));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .6f);
		
		List<Instance> testInstances = getTestInstances(split.get(1));
		
		Instances revisedTestInstances = new Instances(split.get(0));
		revisedTestInstances.clear();
		revisedTestInstances.addAll(testInstances);
		
		for (Classifier c : portfolio) {
			
			c.buildClassifier(split.get(0));
			
			int mistakes = 0;
			
			Evaluation eval = new Evaluation(split.get(0));
			eval.evaluateModel(c, revisedTestInstances);
			System.out.println(eval.pctCorrect());
			
//			for (Instance i : testInstances) {
				
//				System.out.println(resetInstances.iterator().next());
				
//				String prediction = data.classAttribute().value((int)c.classifyInstance(i));
//				String actualValue = i.classAttribute().value((int)i.classValue());
//				double prediction = c.classifyInstance(i);
//				if (!prediction.equals(actualValue)) {
//					mistakes ++;
//				}
//			}
			
//			System.out.println(c.getClass().getName() + ": " + (1 - (mistakes * 1f/split.get(1).size())));
			
		}
		

	}
	
	private List<Instance> getTestInstances(Instances instances) {
		List<Instance> testInstances = new ArrayList<>();
		for (Instance i : instances) {

			ArrayList<Attribute> attributes = new ArrayList<>();
			for (int j = 0; j < instances.numAttributes() - 1; j++) {
				attributes.add(i.attribute(j));
			}
			List<String> values = new ArrayList<>();
			values.add(i.classAttribute().value((int)i.classValue()));
			Attribute classAttribute = new Attribute(i.classAttribute().name(), values);
			attributes.add(classAttribute);
			Instances resetInstances = new Instances("reset", attributes, 1);
			resetInstances.setClassIndex(resetInstances.numAttributes() - 1);
			
			resetInstances.add(new DenseInstance(resetInstances.numAttributes()));
			Instance newInstance = resetInstances.iterator().next();
			for (int j = 0; j < instances.numAttributes() - 1; j++)
				newInstance.setValue(j, i.value(j));
			newInstance.setClassValue(0.0);
//			System.out.println(resetInstances);
//			newInstance.setClassMissing();
			testInstances.add(newInstance);
		}
		return testInstances;
	}
}
