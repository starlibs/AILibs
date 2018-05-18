package ida2018.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.upb.crc901.reduction.single.confusion.ConfusionBasedOptimizingAlgorithm;
import de.upb.crc901.reduction.single.homogeneous.bestofkatrandom.MySQLReductionExperimentRunnerWrapper;
import jaicore.ml.WekaUtil;
import jaicore.ml.classification.multiclass.reduction.MCTreeNodeReD;
import jaicore.ml.evaluation.MulticlassEvaluator;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class ConfusionBasedHeterogeneousStumpEvaluator {
	public static void main(String[] args) throws Exception {
		
		File folder = new File(args[0]);

		/* setup the experiment dimensions */
		int numSeeds = 25;
		List<Integer> seeds = new ArrayList<>();
		for (int seed = 1; seed <= numSeeds; seed++)
			seeds.add(seed);
		Collections.shuffle(seeds);
		List<File> datasetFiles = WekaUtil.getDatasetsInFolder(folder);
		Collections.shuffle(datasetFiles);
		
		int k = 10;
		int mccvRepeats = 20;

		/* conduct next experiments */
		MySQLReductionExperimentRunnerWrapper runner = new MySQLReductionExperimentRunnerWrapper("isys-db.cs.upb.de", "ida2018", "WsFg33sE6aghabMr", "results_reduction", k, mccvRepeats);

		/* launch threads for execution */
		for (int seed : seeds) {
			System.out.println("Considering seed " + seed);
			for (File dataFile : datasetFiles) {
				Instances inst = new Instances(new BufferedReader(new FileReader(dataFile)));
				inst.setClassIndex(inst.numAttributes() - 1);
				List<Instances> split = WekaUtil.getStratifiedSplit(inst, new Random(seed), .7f);
				ConfusionBasedOptimizingAlgorithm algo = new ConfusionBasedOptimizingAlgorithm();
				Collection<String> classifiers = WekaUtil.getBasicLearners();
				classifiers.removeIf(n -> n.contains("Logistic") || n.contains("LMT") || n.contains("Multilayer"));
//				classifiers.clear();
//				classifiers.add("weka.classifiers.bayes.NaiveBayes");
//				classifiers.add("weka.classifiers.bayes.BayesNet");
//				classifiers.add("weka.classifiers.functions.SMO");
				MCTreeNodeReD tree = algo.buildClassifier(split.get(0), classifiers);
				MulticlassEvaluator eval = new MulticlassEvaluator(new Random(seed));
				System.out.println(eval.loss(tree, split.get(1)));
				
			}
		}
	}
}
