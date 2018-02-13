package jaicore.ml.classification.multiclass.reduction;

import java.util.List;

import weka.classifiers.Classifier;

public class MCTreeMergeNode extends MCTreeNodeReD {
  /**
   * Default generated serial version UID.
   */
  private static final long serialVersionUID = -6282530004580334598L;

  public MCTreeMergeNode(final String innerNodeClassifier, final List<String> leftChildClasses, final Classifier leftChildClassifier, final List<String> rightChildClasses,
      final Classifier rightChildClassifier) {
    super(innerNodeClassifier, leftChildClasses, leftChildClassifier, rightChildClasses, rightChildClassifier);
  }

  public MCTreeMergeNode(final Classifier innerNodeClassifier, final List<List<String>> childClasses, final List<Classifier> childClassifier) {
    super(innerNodeClassifier, childClasses, childClassifier);
  }

}
