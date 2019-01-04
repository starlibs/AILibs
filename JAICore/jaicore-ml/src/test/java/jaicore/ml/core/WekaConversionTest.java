package jaicore.ml.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import jaicore.ml.WekaUtil;
import jaicore.ml.interfaces.LabeledInstance;
import jaicore.ml.interfaces.LabeledInstances;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Instance;
import weka.core.Instances;

public class WekaConversionTest {

	private static final File folder = new File("testsrc/ml/orig/");

	@Test
	public void wekaToJAICore() throws Exception {
		Instances inst = new Instances(new BufferedReader(new FileReader(folder + File.separator + "vowel.arff")));
		inst.setClassIndex(inst.numAttributes() - 1);
		jaicore.ml.interfaces.Instances inst2 = WekaUtil.toJAICoreInstances(inst);
		Assert.assertEquals(inst2.getNumberOfColumns(), inst.numAttributes());
		Assert.assertEquals(inst.size(), inst2.size());

		/* write JSON to file */
		try (FileWriter fw = new FileWriter(folder.getParent() + File.separator + "transformed/vowel_unlabeled.json")) {
			fw.write(inst2.toJson());
		}
	}

	@Test
	public void wekaToLabeledJAICore() throws Exception {
		Instances inst = new Instances(new BufferedReader(new FileReader(folder + File.separator + "vowel.arff")));
		inst.setClassIndex(inst.numAttributes() - 1);
		jaicore.ml.interfaces.LabeledInstances<String> inst2 = WekaUtil.toJAICoreLabeledInstances(inst);
		Assert.assertEquals(inst2.getNumberOfColumns() + 1, inst.numAttributes());
		Assert.assertEquals(inst.size(), inst2.size());

		try (FileWriter fw = new FileWriter(folder.getParent() + File.separator + "transformed/vowel_labeled.json")) {
			fw.write(inst2.toJson());
		}
	}

	@Test
	public void wekaInstanceToLabeledJAICore() throws Exception {
		Instances inst = new Instances(new BufferedReader(new FileReader(folder + File.separator + "vowel.arff")));
		inst.setClassIndex(inst.numAttributes() - 1);
		Instance i = inst.iterator().next();

		jaicore.ml.interfaces.LabeledInstance<String> i2 = WekaUtil.toJAICoreLabeledInstance(i);
		Assert.assertEquals(i2.getNumberOfColumns() + 1, inst.numAttributes());
		//Assert.assertNotEquals(i.classValue(), Double.NaN);
		Assert.assertEquals(i.classAttribute().value((int) i.classValue()), i2.getLabel());

		/* back and forth ... */
		WekaCompatibleInstancesImpl tmpInstances = (WekaCompatibleInstancesImpl) WekaUtil.toJAICoreLabeledInstances(inst);
		Assert.assertEquals(WekaUtil.getClassesDeclaredInDataset(inst), tmpInstances.getDeclaredClasses());
		Instances retrievedInstances = WekaUtil.fromJAICoreInstances(tmpInstances);
		Assert.assertEquals(tmpInstances.getDeclaredClasses(), WekaUtil.getClassesDeclaredInDataset(retrievedInstances));
		Assert.assertEquals(inst.size(), retrievedInstances.size());
		Assert.assertEquals(inst.classIndex(), retrievedInstances.classIndex());
	}

	@Test
	public void LabeledJAICoreInstancesToWeka() throws Exception {
		WekaCompatibleInstancesImpl instances = new WekaCompatibleInstancesImpl(new File(folder + File.separator + "vowel_labeled.json"));

		Instances wekaInstances = WekaUtil.fromJAICoreInstances(instances);
		Assert.assertEquals(instances.getNumberOfColumns() + 1, wekaInstances.numAttributes());
		Assert.assertEquals(instances.size(), wekaInstances.size());
		Set<String> labels = new HashSet<>(WekaUtil.getClassesActuallyContainedInDataset(wekaInstances));
		Assert.assertEquals(labels, new HashSet<>(instances.getOccurringLabels()));
		for (Instance inst : wekaInstances) {
			Assert.assertNotNull(inst.dataset());
		}

		/* back and forth ... */
		Assert.assertEquals(instances, WekaUtil.toJAICoreLabeledInstances(WekaUtil.fromJAICoreInstances(instances)));
	}

	@Test
	public void UnlabeledJAICoreInstancesToWeka() throws Exception {
		jaicore.ml.interfaces.Instances instances = new SimpleInstancesImpl(new File(folder + File.separator + "vowel_unlabeled.json"));

		Instances wekaInstances = WekaUtil.fromJAICoreInstances(instances);
		Assert.assertEquals(instances.getNumberOfColumns(), wekaInstances.numAttributes());
		Assert.assertEquals(instances.size(), wekaInstances.size());

		/* back and forth ... */
		Assert.assertEquals(instances, WekaUtil.toJAICoreInstances(WekaUtil.fromJAICoreInstances(instances)));
	}

	@Test
	public void LabeledJAICoreInstanceToWeka() throws Exception {
		LabeledInstances<String> instances = new WekaCompatibleInstancesImpl(new File(folder + File.separator + "vowel_labeled.json"));

		/* get actual instance to be converted */
		LabeledInstance<String> instance = instances.get(12);
		System.out.println(instance.getNumberOfColumns());
		Instance wekaInstance = WekaUtil.fromJAICoreInstance(instance);
		//Assert.assertNotEquals(wekaInstance.classIndex(), -1);
		Assert.assertEquals(instance.getNumberOfColumns() + 1, wekaInstance.numAttributes());
		Assert.assertNotNull(wekaInstance.dataset());
		//Assert.assertNotEquals(wekaInstance.classValue(), Double.NaN);
		Assert.assertEquals(wekaInstance.classAttribute().value((int) wekaInstance.classValue()), instance.getLabel());
	}

	@Test
	public void JAICoreInstanceToWeka() throws Exception {
		jaicore.ml.interfaces.Instances instances = new SimpleInstancesImpl(new File(folder + File.separator + "vowel_unlabeled.json"));
		jaicore.ml.interfaces.Instance instance = instances.get(0);
		Instance wekaInstance = WekaUtil.fromJAICoreInstance(instance);
		Assert.assertEquals(instance.getNumberOfColumns(), wekaInstance.numAttributes());
		Assert.assertNotNull(wekaInstance.dataset());
	}

	@Test
	public void serializeUnserializeLabeledJAICoreInstance() throws Exception {
		LabeledInstances<String> instances = new WekaCompatibleInstancesImpl(new File(folder + File.separator + "vowel_labeled.json"));
		LabeledInstance<String> instance = instances.get(12);
		String json = instance.toJson();
		LabeledInstance<String> newInstance = new SimpleLabeledInstanceImpl(json);
		Assert.assertEquals(newInstance, instance);
	}

	@Test
	public void serializeUnserializeLabeledJAICoreInstances() throws Exception {
		LabeledInstances<String> instances = new WekaCompatibleInstancesImpl(new File(folder + File.separator + "vowel_labeled.json"));
		String json = instances.toJson();
		LabeledInstances<String> newInstances = new WekaCompatibleInstancesImpl(json);
		Assert.assertEquals(newInstances, instances);
	}

	@Test
	public void serializeUnserializeJAICoreInstance() throws Exception {
		jaicore.ml.interfaces.Instances instances = new SimpleInstancesImpl(new File(folder + File.separator + "vowel_unlabeled.json"));
		jaicore.ml.interfaces.Instance instance = instances.get(12);
		String json = instance.toJson();
		jaicore.ml.interfaces.Instance newInstance = new SimpleInstanceImpl(json);
		Assert.assertEquals(newInstance, instance);
	}

	@Test
	public void serializeUnserializeJAICoreInstances() throws Exception {
		jaicore.ml.interfaces.Instances instances = new SimpleInstancesImpl(new File(folder + File.separator + "vowel_unlabeled.json"));
		String json = instances.toJson();
		jaicore.ml.interfaces.Instances newInstances = new SimpleInstancesImpl(json);
		Assert.assertEquals(newInstances, instances);
	}

	@Test
	public void checkClassifierPerformance() throws Exception {
		Instances data = new Instances(new BufferedReader(new FileReader(folder + File.separator + "vowel.arff")));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, System.currentTimeMillis(), .9f);
		Instances instOrig = split.get(0);
		Instances instTranslated = WekaUtil.fromJAICoreInstances(WekaUtil.toJAICoreLabeledInstances(instOrig));
		Instances instTest = split.get(1);

		Classifier[] portfolio = { new BayesNet(), new NaiveBayes(), new SimpleLogistic(), new IBk(), new KStar(), new DecisionTable(), new JRip(), new OneR(),
//				new PART(),
				new ZeroR(), new DecisionStump(),
//				new J48(),
				new LMT(), new RandomForest(), new RandomTree(),
				// new REPTree(),
//				new Logistic(),
//				new MultilayerPerceptron()
				};

		for (Classifier c : portfolio) {
			c.buildClassifier(instOrig);
			Evaluation eval1 = new Evaluation(instOrig);
			eval1.evaluateModel(c, instTest);
			double s1 = eval1.pctCorrect();
			c.buildClassifier(instTranslated);
			Evaluation eval2 = new Evaluation(instOrig);
			eval2.evaluateModel(c, instTest);
			double s2 = eval1.pctCorrect();
			Assert.assertEquals(s1, s2, 0.01);
		}
	}
}