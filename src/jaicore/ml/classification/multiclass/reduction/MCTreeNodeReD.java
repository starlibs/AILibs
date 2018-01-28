package jaicore.ml.classification.multiclass.reduction;

import jaicore.basic.StringUtil;
import jaicore.ml.MajorityClassifier;
import jaicore.ml.WekaUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WekaException;

public class MCTreeNodeReD implements Classifier, Serializable {
  /**
   * Automatically generated serial version UID.
   */
  private static final long serialVersionUID = 8873192747068561266L;

  private class ChildNode {
    private List<String> containedClasses;
    private Classifier childNodeClassifier;

    private ChildNode(final List<String> containedClasses, final Classifier childNodeClassifier) {
      this.containedClasses = containedClasses;
      this.childNodeClassifier = childNodeClassifier;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();

      if (this.childNodeClassifier instanceof MCTreeNodeReD) {
        sb.append(this.childNodeClassifier.toString());
      } else {
        sb.append(this.childNodeClassifier.getClass().getSimpleName() + "(");
        sb.append(StringUtil.implode(this.containedClasses, ","));
        sb.append(")");
      }

      return sb.toString();
    }

    public String toStringWithOffset(final String offset) {
      StringBuilder sb = new StringBuilder();

      if (this.childNodeClassifier instanceof MCTreeNodeReD) {
        sb.append(((MCTreeNodeReD) this.childNodeClassifier).toStringWithOffset(offset + "\t"));
      } else {
        sb.append(offset);
        sb.append("(");
        sb.append(this.containedClasses);
        sb.append(":");
        sb.append(this.childNodeClassifier.getClass().getSimpleName());
        sb.append(")");
      }

      return sb.toString();
    }
  }

  /**
   * Classifier assigned to this inner node.
   */
  private Classifier innerNodeClassifier;
  /**
   * Classes contained in this node.
   */
  private List<String> containedClasses;
  /**
   * List of children of this tree node.
   */
  private List<ChildNode> children = new ArrayList<>();
  /**
   * Flag indicating whether this node is already trained.
   */
  private boolean trained = false;

  public MCTreeNodeReD(final Classifier innerNodeClassifier, final List<String> leftChildClasses, final Classifier leftChildClassifier, final List<String> rightChildClasses,
      final Classifier rightChildClassifier) {
    this.containedClasses = new LinkedList<>();
    this.innerNodeClassifier = innerNodeClassifier;
    this.addChild(leftChildClasses, leftChildClassifier);
    this.addChild(rightChildClasses, rightChildClassifier);
  }

  public MCTreeNodeReD(final Classifier innerNodeClassifier, final List<List<String>> childClasses, final List<Classifier> childClassifier) {
    if (childClasses.size() != childClassifier.size()) {
      throw new IllegalArgumentException("Number of child classes does not equal the number of child classifiers");
    }
    this.innerNodeClassifier = innerNodeClassifier;
    for (int i = 0; i < childClasses.size(); i++) {
      this.addChild(childClasses.get(i), childClassifier.get(i));
    }
  }

  protected MCTreeNodeReD() {

  }

  public void addChild(final List<String> childClasses, final Classifier childClassifier) {
    if (childClassifier instanceof MCTreeMergeNode) {
      this.children.addAll(((MCTreeMergeNode) childClassifier).getChildren());
    } else {
      this.children.add(new ChildNode(childClasses, childClassifier));
    }
  }

  /**
   * @return Returns a list of the child nodes of this node.
   */
  public List<ChildNode> getChildren() {
    return this.children;
  }

  /**
   * Get the classes contained in the leaves of this node.
   *
   * @return Returns a collection of the contained class values contained in the leaves of this node.
   */
  public List<String> getContainedClasses() {
    return this.containedClasses;
  }

  public boolean isCompletelyConfigured() {
    if (this.innerNodeClassifier == null || this.children.isEmpty()) {
      return false;
    }
    for (ChildNode child : this.children) {
      if (child.childNodeClassifier instanceof MCTreeNodeReD) {
        if (!((MCTreeNodeReD) child.childNodeClassifier).isCompletelyConfigured()) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public void buildClassifier(final Instances data) throws Exception {
    assert !data.isEmpty() : "Cannot train MCTree with empty set of instances.";
    assert !this.children.isEmpty() : "Cannot train MCTree without children";

    this.containedClasses.clear();
    for (int i = 0; i < data.numClasses(); i++) {
      this.containedClasses.add(data.classAttribute().value(i));
    }

    // sort class split into clusters
    List<Set<String>> instancesCluster = new ArrayList<>();

    // create subsets of the training data filtering for the respective class values and build child
    // classifier
    for (ChildNode child : this.getChildren()) {
      assert (!child.containedClasses.isEmpty()) : "Contained classes of child must not be empty";
      Instances childData = WekaUtil.getEmptySetOfInstancesWithRefactoredClass(data, child.containedClasses);
      for (Instance i : data) {
        String className = i.classAttribute().value((int) Math.round(i.classValue()));
        if (child.containedClasses.contains(className)) {
          Instance iNew = WekaUtil.getRefactoredInstance(i, child.containedClasses);
          iNew.setClassValue(className);
          iNew.setDataset(childData);
          childData.add(iNew);
        }
      }
      // System.out.println(childData);
      child.childNodeClassifier.buildClassifier(childData);
      instancesCluster.add(new HashSet<>(child.containedClasses));
    }

    // refactor training data with respect to the split clusters and build the classifier
    Instances trainingData = WekaUtil.mergeClassesOfInstances(data, instancesCluster);
    try {
      this.innerNodeClassifier.buildClassifier(trainingData);
    } catch (WekaException e) {
      this.innerNodeClassifier = new MajorityClassifier();
      this.innerNodeClassifier.buildClassifier(trainingData);
    }

    this.trained = true;
  }

  @Override
  public double classifyInstance(final Instance instance) throws Exception {
    System.out.println(instance.classAttribute());
    System.out.println(this.containedClasses);
    double selection = -1;
    double best = 0;
    double[] dist = this.distributionForInstance(instance);
    for (int i = 0; i < dist.length; i++) {
      double score = dist[i];
      if (score > best) {
        best = score;
        selection = i;
      }
    }
    return selection;
  }

  @Override
  public double[] distributionForInstance(final Instance instance) throws Exception {
    assert this.trained : "Cannot get distribution from untrained classifier " + this.toStringWithOffset();

    double[] classDistribution = new double[this.getContainedClasses().size()];

    // compute distribution of the children clusters of the inner node's classifier
    Instance refactoredInstance = WekaUtil.getRefactoredInstance(instance);
    double[] innerNodeClassifierDistribution = this.innerNodeClassifier.distributionForInstance(refactoredInstance);

    // recursively compute distribution for instance for all the children and assign the probabilities
    // to classDistribution array
    for (int childIndex = 0; childIndex < this.children.size(); childIndex++) {
      ChildNode child = this.children.get(childIndex);
      double[] childDistribution = child.childNodeClassifier.distributionForInstance(WekaUtil.getRefactoredInstance(instance, child.containedClasses));
      for (int i = 0; i < childDistribution.length; i++) {
        String classValue = child.containedClasses.get(i);
        classDistribution[this.getContainedClasses().indexOf(classValue)] = childDistribution[i] * innerNodeClassifierDistribution[childIndex];
      }
    }

    double sum = Arrays.stream(classDistribution).sum();
    assert (sum == 1.0) : "Distribution does not sum up to 1; actual some of distribution entries: " + sum;

    return classDistribution;
  }

  @Override
  public Capabilities getCapabilities() {
    return this.innerNodeClassifier.getCapabilities();
  }

  public int getHeight() {
    int maxHeightChildren = 0;
    for (ChildNode child : this.children) {
      if (child.childNodeClassifier instanceof MCTreeNodeReD) {
        maxHeightChildren = Math.max(((MCTreeNodeReD) child.childNodeClassifier).getHeight(), maxHeightChildren);
      }
    }
    return 1 + maxHeightChildren;
  }

  public int getDepthOfFirstCommonParent(final List<String> classes) {
    for (ChildNode child : this.children) {
      if (child.containedClasses.containsAll(classes)) {
        int depth = 1;
        if (child.childNodeClassifier instanceof MCTreeNodeReD) {
          depth += ((MCTreeNodeReD) child.childNodeClassifier).getDepthOfFirstCommonParent(classes);
        }
        return depth;
      }
    }
    return 1;
  }

  public Classifier getClassifier() {
    return this.innerNodeClassifier;
  }

  public void setBaseClassifier(final Classifier classifier) {
    assert classifier != null : "Cannot set null classifier!";
    this.innerNodeClassifier = classifier;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("(");
    sb.append(this.innerNodeClassifier.getClass().getSimpleName());
    sb.append(")");

    sb.append("{");

    boolean first = true;
    for (ChildNode child : this.children) {
      if (first) {
        first = false;
      } else {
        sb.append(",");
      }

      sb.append(child);
    }
    sb.append("}");
    return sb.toString();
  }

  public String toStringWithOffset() {
    return this.toStringWithOffset("");
  }

  public String toStringWithOffset(final String offset) {
    StringBuilder sb = new StringBuilder();

    sb.append(offset);
    sb.append("(");
    sb.append(this.getContainedClasses());
    sb.append(":");
    sb.append(this.innerNodeClassifier.getClass().getSimpleName());
    sb.append(") {");
    boolean first = true;
    for (ChildNode child : this.children) {
      if (first) {
        first = false;
      } else {
        sb.append(",");
      }
      sb.append("\n");
      sb.append(child.toStringWithOffset(offset + "  "));
    }
    sb.append("\n");
    sb.append(offset);
    sb.append("}");
    return sb.toString();
  }

}
