package jaicore.ml.classification.multiclass.reduction;

import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class MCTreeNodeReDLeaf extends MCTreeNodeReD {

  /**
   *
   */
  private static final long serialVersionUID = 6991944564830487953L;

  private String classValue;

  public MCTreeNodeReDLeaf(final String classValue) {
    this.classValue = classValue;
  }

  @Override
  public void addChild(final List<String> childClasses, final Classifier childClassifier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void buildClassifier(final Instances data) throws Exception {
    // intentionally do nothing
  }

  @Override
  public double classifyInstance(final Instance instance) throws Exception {
    return 0.0;
  }

  @Override
  public double[] distributionForInstance(final Instance instance) throws Exception {
    return new double[] { 1.0 };
  }

  @Override
  public int getHeight() {
    return 0;
  }

  @Override
  public Capabilities getCapabilities() {
    return null;
  }

  @Override
  public String toString() {
    return this.classValue;
  }

  @Override
  public boolean isCompletelyConfigured() {
    return true;
  }

  @Override
  public String toStringWithOffset(final String offset) {
    StringBuilder sb = new StringBuilder();

    sb.append(offset);
    sb.append("(");
    sb.append(this.getContainedClasses());
    sb.append(")");
    return sb.toString();
  }
}
