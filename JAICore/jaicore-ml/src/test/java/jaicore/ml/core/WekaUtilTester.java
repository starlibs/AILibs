package jaicore.ml.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class WekaUtilTester {

	private static final File BASE_FOLDER = new File("testrsc/ml/orig/");
	private static final File VOWEL_ARFF = new File(BASE_FOLDER, "vowel.arff");

	Classifier[] portfolio = {
			// new BayesNet(), new NaiveBayes(),
			// new SimpleLogistic(),
			// new IBk(), new KStar(),
			// new DecisionTable(),
			// new JRip(), new OneR(),
			// new PART(),
			// new ZeroR(), new DecisionStump(), new J48(),
			// new LMT(),
			new RandomForest(),
			// new RandomTree(),
			// new REPTree(),
			// new Logistic(),
			// new MultilayerPerceptron()
	};

	@Test
	public void checkSplit() throws Exception {

		Instances inst = new Instances(new BufferedReader(new FileReader(VOWEL_ARFF)));
		inst.setClassIndex(inst.numAttributes() - 1);
		for (Classifier c : this.portfolio) {

			/* eval for CV */
			inst.stratify(10);
			Instances train = inst.trainCV(10, 0);
			Instances test = inst.testCV(10, 0);
			Assert.assertEquals(train.size() + test.size(), inst.size());
			Evaluation eval = new Evaluation(train);
			eval.crossValidateModel(c, inst, 10, new Random(0));

			c.buildClassifier(train);
			eval.evaluateModel(c, test);
			System.out.println(eval.pctCorrect());
		}
	}

	@Test
	public void checkValidAttributeSelections() throws Exception {
		Collection<List<String>> preprocessors = WekaUtil.getAdmissibleSearcherEvaluatorCombinationsForAttributeSelection();
		preprocessors.forEach(a -> System.out.println(a.toString()));
	}

	public static void main(final String[] args) throws Exception {
		new WekaUtilTester().checkValidAttributeSelections();
	}
}
