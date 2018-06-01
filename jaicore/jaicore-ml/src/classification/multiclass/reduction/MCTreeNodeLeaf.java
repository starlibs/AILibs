package jaicore.ml.classification.multiclass.reduction;

import java.util.Arrays;

import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class MCTreeNodeLeaf extends MCTreeNode {

  /**
   *
   */
  private static final long serialVersionUID = 6991944564830487953L;

  private int classIndex;

  public MCTreeNodeLeaf(final int classIndex) {
    super(Arrays.asList(new Integer[] { classIndex }));
    this.classIndex = classIndex;
  }

  @Override
  public void addChild(final MCTreeNode newNode) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void buildClassifier(final Instances data) throws Exception {
    // intentionally do nothing
  }

  @Override
  public double classifyInstance(final Instance instance) throws Exception {
    return this.classIndex;
  }

  @Override
  public void distributionForInstance(final Instance instance, final double[] distribution) throws Exception {
    distribution[this.classIndex] = 1;
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
    return this.classIndex + "";
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
