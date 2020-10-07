package ai.libs.jaicore.ml.weka.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstance;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.test.MediumTest;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class WekaUtilTest extends ATest {

	private static final File BASE_FOLDER = new File("testrsc/ml/orig/");
	private static final File VOWEL_ARFF = new File(BASE_FOLDER, "vowel.arff");
	private static final int OPENML_KRVSKP = 3;

	private final Classifier[] portfolio = {
			// new BayesNet(), new NaiveBayes(),
			// new SimpleLogistic(),
			// new IBk(), new KStar(),
			// new DecisionTable(),
			// new JRip(), new OneR(),
			// new PART(),
			// new ZeroR(), new DecisionStump(), new J48(),
			// new LMT(),
			new RandomForest()
			// new RandomTree(),
			// new REPTree(),
			// new Logistic(),
			// new MultilayerPerceptron()
	};

	@MediumTest
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
			assertTrue(eval.pctCorrect() >= 0);
		}
	}

	@Test
	public void checkEqualsMethod() throws Exception {

		Instances ds1 = new Instances(new BufferedReader(new FileReader(VOWEL_ARFF)));
		ds1.setClassIndex(ds1.numAttributes() - 1);

		Instances ds2 = new Instances(new BufferedReader(new FileReader(VOWEL_ARFF)));
		ds2.setClassIndex(ds2.numAttributes() - 1);

		int n = ds1.size();
		for (int i = 0; i < n; i++) {
			assertTrue(WekaUtil.areInstancesEqual(ds1.get(i), ds2.get(i)));
		}
	}

	@Test
	@MediumTest
	public void checkDeterminismOfStratifiedSplits() throws Exception {

		Instances data = new Instances(new BufferedReader(new FileReader(VOWEL_ARFF)));
		data.setClassIndex(data.numAttributes() - 1);
		IWekaInstances dataset = new WekaInstances(data);

		long seed = System.currentTimeMillis();
		List<IWekaInstances> split1 = WekaUtil.getStratifiedSplit(dataset, seed, .7f);
		List<IWekaInstances> split2 = WekaUtil.getStratifiedSplit(dataset, seed, .7f);

		IWekaInstances split1a = new WekaInstances(split1.get(0));
		IWekaInstances split2a = new WekaInstances(split2.get(0));
		IWekaInstances split1b = new WekaInstances(split1.get(1));
		IWekaInstances split2b = new WekaInstances(split2.get(1));
		for (IWekaInstance inst : split1a) {
			assertTrue("Instance " + Arrays.toString(inst.getPoint()) + " is only contained in first split but not in the second. " + split2a, split2a.contains(inst));
		}
		for (IWekaInstance inst : split2a) {
			assertTrue("Instance " + Arrays.toString(inst.getPoint()) + " is only contained in second split but not in the first. " + split1a, split1a.contains(inst));
		}
		for (IWekaInstance inst : split1b) {
			assertTrue("Instance " + Arrays.toString(inst.getPoint()) + " is only contained in first split but not in the second. " + split2b, split2b.contains(inst));
		}
		for (IWekaInstance inst : split2b) {
			assertTrue("Instance " + Arrays.toString(inst.getPoint()) + " is only contained in second split but not in the first. " + split1b, split1b.contains(inst));
		}
	}

	@Test
	public void checkValidAttributeSelections() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		Collection<List<String>> preprocessors = WekaUtil.getAdmissibleSearcherEvaluatorCombinationsForAttributeSelection();
		assertEquals(9, preprocessors.size());

		/* maybe check here the executability of those combinations */
	}

}
