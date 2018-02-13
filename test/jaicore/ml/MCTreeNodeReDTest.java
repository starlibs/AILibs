package jaicore.ml;

import jaicore.ml.classification.multiclass.reduction.MCTreeNodeReD;

import java.io.FileReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class MCTreeNodeReDTest {

  private static final String CLASSIFIER_NAME = RandomForest.class.getName();

  public static void main(final String[] args) throws Exception {
    System.out.println(CLASSIFIER_NAME);

    Instances data = new Instances(new FileReader("testrsc/autowekasets/dataset_189_baseball.arff"));
    data.setClassIndex(data.numAttributes() - 1);

    for (int s = 0; s < 10; s++) {
      List<Instances> stratifiedSplit = WekaUtil.getStratifiedSplit(data, new Random(s), 0.7);

      List<String> classValues = new LinkedList<>();
      for (int i = 0; i < data.numClasses(); i++) {
        classValues.add(data.classAttribute().value(i));
      }

      List<Double> pctCorrectClassifier = new LinkedList<>();
      List<Double> pctCorrectDecomposition = new LinkedList<>();
      List<Classifier> ensemble = new LinkedList<>();

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
          childAClassifier = new MajorityClassifier();
        }
        Classifier childBClassifier;
        if (childB.size() > 1) {
          childBClassifier = AbstractClassifier.forName(CLASSIFIER_NAME, null);
        } else {
          childBClassifier = new MajorityClassifier();
        }

        MCTreeNodeReD root = new MCTreeNodeReD(AbstractClassifier.forName(CLASSIFIER_NAME, null), childA, childAClassifier, childB, childBClassifier);
        ensemble.add(root);
        root.buildClassifier(stratifiedSplit.get(0));

        // for (Instance test : stratifiedSplit.get(1)) {
        // System.out.println(root.classifyInstance(test) + " " + test.classValue());
        // }

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

      Ensemble e = new Ensemble();
      e.addAll(ensemble);
      e.buildClassifier(stratifiedSplit.get(0));
      Evaluation eval = new Evaluation(data);
      eval.evaluateModel(e, stratifiedSplit.get(1), new Object[] {});

      System.out.println("Ensemble: " + (100 - eval.pctCorrect()));
      System.out.println();
    }

    while (true) {
      ;
    }
  }

}
