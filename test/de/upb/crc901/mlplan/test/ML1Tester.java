package de.upb.crc901.mlplan.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jaicore.ml.WekaUtil;
import jaicore.ml.core.WekaCompatibleInstancesImpl;
import jaicore.ml.interfaces.LabeledInstance;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;
import weka.core.converters.Loader;

public class ML1Tester {

	public static Instances loadOriginal() throws Exception {
		Loader loader = new ArffLoader();
		loader.setSource(new File("testrsc/autowekasets/credit-g.arff"));
		Instances data = loader.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);
		return data;
	}

	public static Instances loadSplitBased() throws Exception {
		

//		jaicore.ml.interfaces.LabeledInstances<String> train = WekaUtil.toJAICoreLabeledInstances(wekaTrain);
//		jaicore.ml.interfaces.LabeledInstances<String> test = WekaUtil.toJAICoreLabeledInstances(wekaTest);

		Instances data = new Instances(getTrainingSet());
		data.addAll(getTestSet());
		return data;
	}
	
	public static Instances getTrainingSet() throws Exception {
		Loader loader = new CSVLoader();
		loader.setSource(new File("testrsc/ml1data.train.txt"));
		Instances wekaTrain = loader.getDataSet();
		wekaTrain.setClassIndex(wekaTrain.numAttributes() - 1);
		return wekaTrain;
	}
	
	public static Instances getTestSet() throws Exception {
		Loader loader = new CSVLoader();
		loader.setSource(new File("testrsc/ml1data.test.txt"));
		Instances wekaTest = loader.getDataSet();
		wekaTest.setClassIndex(wekaTest.numAttributes() - 1);
		return wekaTest;
	}
	
	public static void kFoldMCCV(int k) throws Exception {
		Instances data = loadSplitBased();
		for (int i = 0; i < k; i++) {
			List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(i), .5f);
			Classifier c = new RandomForest();
			System.out.println(evalSplit(c, split.get(0), split.get(1)));
		}
	}
	
	public static double evalSplit(Classifier c, Instances train, Instances test) throws Exception {
		c.buildClassifier(train);
		Evaluation eval = new Evaluation(train);
		eval.evaluateModel(c, test);
		return eval.pctCorrect();
	}

	public static void main(String[] args) throws Exception {

		jaicore.ml.interfaces.LabeledInstances<String> training = WekaUtil.toJAICoreLabeledInstances(getTrainingSet());
		jaicore.ml.interfaces.LabeledInstances<String> test = WekaUtil.toJAICoreLabeledInstances(getTestSet());
		List<String> classes = new ArrayList<>(training.getOccurringLabels());
		WekaCompatibleInstancesImpl data = new WekaCompatibleInstancesImpl(classes);
		for (LabeledInstance<String> i : training)
			data.add(i);
		for (LabeledInstance<String> i : test)
			data.add(i);
		
		/* create true train/test-split */
		Instances wekaData = WekaUtil.fromJAICoreInstances(data);
		Instances wekaTrain = new Instances(wekaData);
		Instances wekaTest = new Instances(wekaData);
		System.out.println("\n\n\n\nASDAD__");
		int currentInTraining = 0;
		int currentInTest = 0;
		for (int i = 0; i < wekaData.size(); i++) {
			Instance inst = wekaData.get(i);
			if (training.contains(WekaUtil.toJAICoreLabeledInstance(inst))) {
				wekaTest.remove(currentInTest);
				currentInTraining ++;
			}
			else {
				wekaTrain.remove(currentInTraining);
				currentInTest ++;
			}
		}

		System.out.println(evalSplit(AbstractClassifier.forName("de.upb.crc901.mlplan.core.MLPlan", new String[] {"-t", "60", "-r", "0"}), wekaTrain, wekaTest));
		
//		for (String cName : WekaUtil.getBasicLearners()) {
//			try {
//			System.out.println(cName + ": " + evalSplit(AbstractClassifier.forName(cName, null), wekaTrain, wekaTest));
//			}
//			catch (Throwable e) {
//				System.err.println("Ignoring " + cName);
//			}
//		}
		
//		kFoldMCCV(10);
//
//		 System.out.println("D1:");
//		 data.stream().forEach(d -> System.out.println("\t" + d));
//		 System.out.println("D2:");
//		 training.stream().forEach(d -> System.out.println("\t" + d));
//		 System.out.println(SetUtil.difference(data, training).size());

		// Instances testData = new Instances(data);
		// testData.clear();

		// System.out.println(train);
		System.exit(0);

		// Classifier rf = new RandomForest();
		// rf.buildClassifier(wekaTrain);
		// loader.setSource(new File("testrsc/ml1data.test.txt"));
		// Instances test = initSplit.get(1);
		// Instances test = loader.getDataSet();
		// test.setClassIndex(test.numAttributes() - 1);
		// test.setClassIndex(data.numAttributes() - 1);
		// Evaluation eval = new Evaluation(wekaTrain);
		// eval.evaluateModel(rf, test);
		// System.out.println(eval.pctIncorrect());

		// List<Instances> initSplit = WekaUtil.getStratifiedSplit(data, new Random(45), .5);
		// Saver saver = new CSVSaver();
		// saver.setFile(new File("testrsc/ml1data.train.txt"));
		// saver.setInstances(initSplit.get(0));
		// saver.writeBatch();
		//
		// saver.setFile(new File("testrsc/ml1data.test.txt"));
		// saver.setInstances(initSplit.get(1));
		// saver.writeBatch();

		System.exit(0);
//		double best = 100;
//		String bestConf = "";
//		for (int exp = 0; exp <= 10000; exp++) {
//			String cName = "weka.classifiers.functions.SMO";
//
//			DescriptiveStatistics intStats = new DescriptiveStatistics();
//			DescriptiveStatistics extStats = new DescriptiveStatistics();
//			try {
//				for (int k = 0; k < 5; k++) {
//					List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(k), .5);
//					// System.out.println(split.get(0));
//
//					System.out.println(cName + " /" + exp);
//
//					Classifier c = AbstractClassifier.forName(cName, new String[] { "-C", "1." + exp });
//					// c.setOptions(new String[] {"-H", "a,a,a,a,a,a,a"});
//					c.buildClassifier(split.get(0));
//					int mistakes = 0;
//					for (Instance i : split.get(0)) {
//						// System.out.println(c.classifyInstance(i) + ": " + i.classValue());
//						if (c.classifyInstance(i) != i.classValue())
//							mistakes++;
//					}
//					intStats.addValue(mistakes * 1f / split.get(0).size());
//					mistakes = 0;
//					for (Instance i : split.get(1)) {
//						// System.out.println(c.classifyInstance(i) + ": " + i.classValue());
//						if (c.classifyInstance(i) != i.classValue())
//							mistakes++;
//					}
//					extStats.addValue(mistakes * 1f / split.get(1).size());
//
//				}
//
//				if (extStats.getMean() < best) {
//					best = extStats.getMean();
//					bestConf = "1." + exp;
//				}
//				System.out.println(intStats.getMean() + " / " + extStats.getMean());
//			} catch (Exception e) {
//				System.err.println("Ignoring " + cName + " due to exception.");
//			}
//			System.out.println("Best is " + best + ". val: " + bestConf);
//		}
	}

}
