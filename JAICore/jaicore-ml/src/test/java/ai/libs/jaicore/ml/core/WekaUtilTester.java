package ai.libs.jaicore.ml.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.ml.cache.InstructionGraph;
import ai.libs.jaicore.ml.cache.LoadDatasetInstructionForOpenML;
import ai.libs.jaicore.ml.cache.ReproducibleInstances;
import ai.libs.jaicore.ml.cache.StratifiedSplitSubsetInstruction;
import ai.libs.jaicore.ml.core.dataset.weka.WekaInstance;
import ai.libs.jaicore.ml.core.dataset.weka.WekaInstances;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class WekaUtilTester {

	private static final File BASE_FOLDER = new File("testrsc/ml/orig/");
	private static final File VOWEL_ARFF = new File(BASE_FOLDER, "vowel.arff");
	private static final int OPENML_KRVSKP = 3;

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
	public void checkDeterminismOfStratifiedSplits() throws Exception {

		Instances data = new Instances(new BufferedReader(new FileReader(VOWEL_ARFF)));
		data.setClassIndex(data.numAttributes() - 1);

		long seed = System.currentTimeMillis();
		List<Instances> split1 = WekaUtil.getStratifiedSplit(data, seed, .7f);
		List<Instances> split2 = WekaUtil.getStratifiedSplit(data, seed, .7f);

		WekaInstances<Object> split1a = new WekaInstances<>(split1.get(0));
		WekaInstances<Object> split2a = new WekaInstances<>(split2.get(0));
		WekaInstances<Object> split1b = new WekaInstances<>(split1.get(1));
		WekaInstances<Object> split2b = new WekaInstances<>(split2.get(1));
		for (WekaInstance<Object> inst : split1a) {
			assertTrue("Instance " + Arrays.toString(inst.getAsDoubleVector()) + " is only contained in first split but not in the second. " + split2a, split2a.contains(inst));
		}
		for (WekaInstance<Object> inst : split2a) {
			assertTrue("Instance " + Arrays.toString(inst.getAsDoubleVector()) + " is only contained in second split but not in the first. " + split1a, split1a.contains(inst));
		}
		for (WekaInstance<Object> inst : split1b) {
			assertTrue("Instance " + Arrays.toString(inst.getAsDoubleVector()) + " is only contained in first split but not in the second. " + split2b, split2b.contains(inst));
		}
		for (WekaInstance<Object> inst : split2b) {
			assertTrue("Instance " + Arrays.toString(inst.getAsDoubleVector()) + " is only contained in second split but not in the first. " + split1b, split1b.contains(inst));
		}
	}

	@Test
	public void checkEqualnessOfDirectAndInstructedStratifiedSplits() throws Exception {

		/* get stratified split for data directly */
		ReproducibleInstances data = ReproducibleInstances.fromOpenML(OPENML_KRVSKP, "");
		data.setClassIndex(data.numAttributes() - 1);
		long seed = System.currentTimeMillis();
		List<ReproducibleInstances> split1 = WekaUtil.getStratifiedSplit(data, seed, .7f);

		/* get stratified split via instruction graph */
		InstructionGraph graph = new InstructionGraph();
		graph.addNode("load", new LoadDatasetInstructionForOpenML("", OPENML_KRVSKP));
		graph.addNode("split", new StratifiedSplitSubsetInstruction(seed, .7f), Arrays.asList(new Pair<>("load", 0)));
		List<WekaInstances<Object>> split2 = new ArrayList<>();
		split2.add(((WekaInstances<Object>)graph.getDataForUnit(new Pair<>("split", 0))));
		split2.add(((WekaInstances<Object>)graph.getDataForUnit(new Pair<>("split", 1))));

		/* check equalness of the two splits */
		assertEquals(new WekaInstances<>(split1.get(0)).size(), split2.get(0).size());
		WekaInstances<Object> split1a = new WekaInstances<>(split1.get(0));
		WekaInstances<Object> split2a = split2.get(0);
		WekaInstances<Object> split1b = new WekaInstances<>(split1.get(1));
		WekaInstances<Object> split2b = split2.get(1);
		System.out.println("now checking containment");
		for (WekaInstance<Object> inst : split1a) {
			assertTrue("Instance " + inst + " is only contained in first split but not in the second. " + split2a, split2a.contains(inst));
		}
		System.out.println("A");
		for (WekaInstance<Object> inst : split2a) {
			assertTrue("Instance " + inst + " is only contained in second split but not in the first. " + split1a, split1a.contains(inst));
		}
		System.out.println("B");
		for (WekaInstance<Object> inst : split1b) {
			assertTrue("Instance " + inst + " is only contained in first split but not in the second. " + split2b, split2b.contains(inst));
		}
		System.out.println("c");
		for (WekaInstance<Object> inst : split2b) {
			assertTrue("Instance " + inst + " is only contained in second split but not in the first. " + split1b, split1b.contains(inst));
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
