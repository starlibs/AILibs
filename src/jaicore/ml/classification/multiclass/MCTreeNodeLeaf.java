package jaicore.ml.classification.multiclass;

import java.util.LinkedList;
import java.util.List;

import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class MCTreeNodeLeaf extends MCTreeNode {

  private int classIndex;

  public MCTreeNodeLeaf(final int classIndex) throws Exception {
    super();
    this.classIndex = classIndex;
  }

  @Override
  public void addChild(final MCTreeNode newNode) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Integer> getContainedClasses() {
    List<Integer> containedClasses = new LinkedList<>();
    containedClasses.add(this.classIndex);
    return containedClasses;
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
  public double[] distributionForInstance(final Instance instance) throws Exception {
    return null;
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
}
