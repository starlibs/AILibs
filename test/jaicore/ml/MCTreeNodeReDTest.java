package jaicore.ml;

import jaicore.ml.classification.multiclass.reduction.ConstantClassifier;
import jaicore.ml.classification.multiclass.reduction.MCTreeNodeReD;

import java.io.FileReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.pmml.jaxbbindings.Con1;
import weka.core.pmml.jaxbbindings.Constant;

public class MCTreeNodeReDTest {

  public static void main(final String[] args) throws Exception {

    Instances data = new Instances(new FileReader("testrsc/autowekasets/shuttle.arff"));
    data.setClassIndex(data.numAttributes() - 1);

    List<Instances> stratifiedSplit = WekaUtil.getStratifiedSplit(data, new Random(1234), 0.8);

    List<String> classValues = new LinkedList<>();
    for (int i = 0; i < data.numClasses(); i++) {
      classValues.add(data.classAttribute().value(i));
    }

    Collections.shuffle(classValues, new Random(123));
    List<String> childA = new LinkedList<>();
    List<String> childB = new LinkedList<>();

    for (int i = 0; i < classValues.size(); i++) {
      if (i < 1) {
        childA.add(classValues.get(i));
      } else {
        childB.add(classValues.get(i));
      }
    }

    Classifier childAClassifier = new ConstantClassifier();
    Classifier childBClassifier = new J48();

    MCTreeNodeReD root = new MCTreeNodeReD(J48.class.getName(), childA, childAClassifier, childB, childBClassifier);
    root.buildClassifier(data);

    // for (Instance test : stratifiedSplit.get(1)) {
    // System.out.println(root.classifyInstance(test) + " " + test.classValue());
    // }

    Evaluation eval = new Evaluation(data);
    eval.evaluateModel(root, stratifiedSplit.get(1), new Object[] {});

    System.out.println(eval.pctCorrect());

  }

}
