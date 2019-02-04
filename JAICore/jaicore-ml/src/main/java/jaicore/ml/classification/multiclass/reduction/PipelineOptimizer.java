package jaicore.ml.classification.multiclass.reduction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import jaicore.basic.MathExt;
import jaicore.ml.WekaUtil;
import jaicore.ml.classification.multiclass.reduction.reducer.ReductionOptimizer;
import weka.classifiers.Classifier;
import weka.classifiers.rules.OneR;
import weka.core.Instance;
import weka.core.Instances;

public class PipelineOptimizer {
	
	public static void main(String[] args) throws Exception {
		File folder = new File("../CrcTaskBasedConfigurator/testrsc/polychotomous/");
		Instances inst = new Instances(new BufferedReader(new FileReader(folder + File.separator + "vowel.arff")));
		inst.setClassIndex(inst.numAttributes() -1);
		
		Classifier mcc = new OneR();
//		mcc.setClassifier(new IBk());
		
		String classifierName = "weka.classifiers.trees.RandomForest";
		
		for (int i = 0; i < inst.classAttribute().numValues(); i++)
			System.out.println(i + ": " + inst.classAttribute().value(i));
		
		for (int j = 0; j < 1; j++) {
			List<Instances> split = WekaUtil.getStratifiedSplit(inst, j, .6f);
			
			System.out.print("Running base learner ...");
//			Classifier rf = AbstractClassifier.forName(classifierName, null);
			mcc.buildClassifier(split.get(0));
			System.out.println("done. Accuracy: " + getAccuracy(mcc, split.get(1)) + "%");
			
			Classifier c = new ReductionOptimizer(j);
			
//			System.out.print("Train ... ");
			c.buildClassifier(split.get(0));
//			System.out.println("done");
			System.out.println("MOD: " + getAccuracy(c, split.get(1)) + "%");
		}
	}
	
	private static double getAccuracy(Classifier c, Instances test) throws Exception {
		int mistakes = 0;
		for (Instance i : test) {
			if (c.classifyInstance(i) != i.classValue())
				mistakes ++;
		}
		return MathExt.round(100 * (1- mistakes * 1f / test.size()), 2);
	}
}
