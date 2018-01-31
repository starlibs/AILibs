import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

import jaicore.ml.WekaUtil;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class Test {

	public static void main(String[] args) throws Exception {
		Instances inst = new Instances(new BufferedReader(new FileReader(new File("testrsc/all/amazon.arff"))));
		inst.setClassIndex(inst.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(inst, new Random(0), .7f);
		Classifier c = AbstractClassifier.forName("weka.classifiers.trees.RandomForest", new String[] {"-P", "100", "-I", "4", "-num-slots", "1", "-K", "2", "-M", "1.0", "-V", "0.001", "-S", "1", "-depth", "1"});
		System.out.print("Start training ... ");
		c.buildClassifier(split.get(0));
		System.out.println("done");
	}

}
