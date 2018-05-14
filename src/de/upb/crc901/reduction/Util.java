package de.upb.crc901.reduction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jaicore.basic.SetUtil;
import jaicore.ml.WekaUtil;
import jaicore.ml.classification.multiclass.reduction.MCTreeNodeReD;
import jaicore.ml.classification.multiclass.reduction.splitters.RPNDSplitter;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class Util {

	public static Collection<List<String>> getReductionStumpCombinations() {
		Collection<String> classifiers = WekaUtil.getBasicLearners();
		Collection<List<String>> classifierCombos;
		try {
			classifierCombos = SetUtil.cartesianProduct(classifiers, 3);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		return classifierCombos;
	}

	public static List<Map<String, Object>> conductExperiment(ReductionExperiment experiment) throws Exception {

		/* load data */
		Instances data = new Instances(new BufferedReader(new FileReader(experiment.getDataset())));
		data.setClassIndex(data.numAttributes() - 1);

		/* prepare basis for experiments */
		int seed = experiment.getSeed();
		Classifier classifierForRPNDSplit = AbstractClassifier.forName(experiment.getNameOfClassifierForRPNDSplit(), null);
		Classifier leftClassifier = AbstractClassifier.forName(experiment.getNameOfLeftClassifier(), null);
		Classifier innerClassifier = AbstractClassifier.forName(experiment.getNameOfInnerClassifier(), null);
		Classifier rightClassifier = AbstractClassifier.forName(experiment.getNameOfRightClassifier(), null);
		Collection<String> classes = WekaUtil.getClassesActuallyContainedInDataset(data);
		Random splitRandomSource = new Random(seed);
		RPNDSplitter splitter = new RPNDSplitter(data, new Random(seed));

		/* conduct experiments */
		List<Map<String, Object>> results = new ArrayList<>();
		for (int k = 0; k < 10; k++) {
			List<Collection<String>> classSplit;
			try {
				classSplit = new ArrayList<>(splitter.split(classes, classifierForRPNDSplit));
			} catch (Throwable e) {
				throw new RuntimeException("Could not create RPND split.", e);
			}
			MCTreeNodeReD classifier = new MCTreeNodeReD(innerClassifier, classSplit.get(0), leftClassifier, classSplit.get(1), rightClassifier);
			long start = System.currentTimeMillis();
			Map<String, Object> result = new HashMap<>();
			List<Instances> dataSplit = WekaUtil.getStratifiedSplit(data, splitRandomSource, .7f);
			classifier.buildClassifier(dataSplit.get(0));
			Evaluation eval = new Evaluation(dataSplit.get(0));
			eval.evaluateModel(classifier, dataSplit.get(1));
			double loss = (100 - eval.pctCorrect()) / 100f;
			result.put("errorRate", loss);
			result.put("trainTime", System.currentTimeMillis() - start);
			results.add(result);
		}
		return results;
	}
}
