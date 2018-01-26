package de.upb.crc901.mlplan.test;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import jaicore.ml.WekaUtil;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;
import weka.core.converters.Loader;
import weka.core.converters.Saver;

public class ML1Tester {

	public static void main(String[] args) throws Exception {

		// load CSV
		Loader loader = new CSVLoader();
		loader.setSource(new File("testrsc/ml1data.txt"));
		// Loader loader = new ArffLoader();
		// loader.setSource(new File("testrsc/autowekasets/credit-g.arff"));
		Instances data = loader.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> initSplit = WekaUtil.getStratifiedSplit(data, new Random(45), .5);
		
		Instances train = initSplit.get(0);
		System.out.println(train);
		
		
		Classifier rf = new RandomForest();
		rf.buildClassifier(train);
		loader.setSource(new File("testrsc/ml1data.test.txt"));
//		Instances test = initSplit.get(1);
		Instances test = loader.getDataSet();
		test.setClassIndex(test.numAttributes() - 1);
//		test.setClassIndex(data.numAttributes() - 1);
		Evaluation eval = new Evaluation(train);
		eval.evaluateModel(rf, test);
		System.out.println(eval.pctIncorrect());
		
		
//		List<Instances> initSplit = WekaUtil.getStratifiedSplit(data, new Random(45), .5);
//		Saver saver = new CSVSaver();
//		saver.setFile(new File("testrsc/ml1data.train.txt"));
//		saver.setInstances(initSplit.get(0));
//		saver.writeBatch();
//		
//		saver.setFile(new File("testrsc/ml1data.test.txt"));
//		saver.setInstances(initSplit.get(1));
//		saver.writeBatch();
		
		System.exit(0);
		double best = 100;
		String bestConf = "";
		for (int exp = 0; exp <= 10000; exp ++) {
			String cName = "weka.classifiers.functions.SMO";
			
			DescriptiveStatistics intStats = new DescriptiveStatistics();
			DescriptiveStatistics extStats = new DescriptiveStatistics();
			try {
				for (int k = 0; k < 5; k++) {
					List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(k), .5);
					// System.out.println(split.get(0));

					System.out.println(cName + " /" + exp);

					Classifier c = AbstractClassifier.forName(cName, new String[] {"-C" , "1." + exp});
					// c.setOptions(new String[] {"-H", "a,a,a,a,a,a,a"});
					c.buildClassifier(split.get(0));
					int mistakes = 0;
					for (Instance i : split.get(0)) {
						// System.out.println(c.classifyInstance(i) + ": " + i.classValue());
						if (c.classifyInstance(i) != i.classValue())
							mistakes++;
					}
					intStats.addValue(mistakes * 1f / split.get(0).size());
					mistakes = 0;
					for (Instance i : split.get(1)) {
						// System.out.println(c.classifyInstance(i) + ": " + i.classValue());
						if (c.classifyInstance(i) != i.classValue())
							mistakes++;
					}
					extStats.addValue(mistakes * 1f / split.get(1).size());

				}
				
				if (extStats.getMean() < best) {
					best = extStats.getMean();
					bestConf = "1." + exp;
				}
				System.out.println(intStats.getMean() + " / " + extStats.getMean());
			} catch (Exception e) {
				System.err.println("Ignoring " + cName + " due to exception.");
			}
			System.out.println("Best is " + best + ". val: " + bestConf);
		}
	}

}
