package jaicore.ml;

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import jaicore.ml.classification.multiclass.reduction.MCTreeNodeReD;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

/**
 * In this class it is checked whether the MCTreeNodeReD, i.e. the reduction stump classifier is working properly.
 *
 * @author fmohr, mwever
 */
public class MCTreeNodeReDTest {

	private static final String CLASSIFIER_NAME = RandomForest.class.getName();
	private static final File DATASET = new File("testrsc/orig/vowel.arff");

	public static void main(final String[] args) throws Exception {
		System.out.println(CLASSIFIER_NAME);

		Instances data = new Instances(new FileReader(DATASET));
		data.setClassIndex(data.numAttributes() - 1);

		for (int s = 0; s < 10; s++) {
			List<Instances> stratifiedSplit = WekaUtil.getStratifiedSplit(data, s, 0.7);

			List<String> classValues = new LinkedList<>();
			for (int i = 0; i < data.numClasses(); i++) {
				classValues.add(data.classAttribute().value(i));
			}

			List<Double> pctCorrectClassifier = new LinkedList<>();
			List<Double> pctCorrectDecomposition = new LinkedList<>();

			for (int k = 0; k < 10; k++) {
				Collections.shuffle(classValues, new Random(k));
				List<String> childA = new LinkedList<>();
				List<String> childB = new LinkedList<>();

				for (int i = 0; i < classValues.size(); i++) {
					if (i < classValues.size() / 2) {
						childA.add(classValues.get(i));
					} else {
						childB.add(classValues.get(i));
					}
				}

				Classifier childAClassifier;
				if (childA.size() > 1) {
					childAClassifier = AbstractClassifier.forName(CLASSIFIER_NAME, null);
				} else {
					childAClassifier = new ZeroR();
				}
				Classifier childBClassifier;
				if (childB.size() > 1) {
					childBClassifier = AbstractClassifier.forName(CLASSIFIER_NAME, null);
				} else {
					childBClassifier = new ZeroR();
				}

				MCTreeNodeReD root = new MCTreeNodeReD(CLASSIFIER_NAME, childA, childAClassifier, childB, childBClassifier);
				root.buildClassifier(stratifiedSplit.get(0));

				Evaluation eval = new Evaluation(data);
				eval.evaluateModel(root, stratifiedSplit.get(1), new Object[] {});

				double decomposition = eval.pctCorrect();
				pctCorrectDecomposition.add(decomposition);

				Classifier c = AbstractClassifier.forName(CLASSIFIER_NAME, null);
				c.buildClassifier(stratifiedSplit.get(0));
				eval.evaluateModel(c, stratifiedSplit.get(1), new Object[] {});
				pctCorrectClassifier.add(eval.pctCorrect());

				double maxCorrectDec = pctCorrectDecomposition.stream().mapToDouble(x -> x).max().getAsDouble();
				double maxCorrectCls = pctCorrectClassifier.stream().mapToDouble(x -> x).max().getAsDouble();

				System.out.println((maxCorrectCls - maxCorrectDec) + " (Decomp: " + (100 - maxCorrectDec) + "/Classifier: " + (100 - maxCorrectCls) + ")");
			}

		}

	}

}
