package jaicore.ml.classification.multiclass;

import jaicore.ml.WekaUtil;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.meta.MultiClassClassifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class MCTreeNode implements Classifier, ITreeClassifier, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 8873192747068561266L;

  private EMCNodeType nodeType;
  private List<MCTreeNode> children;
  private Classifier classifier;
  private String classifierID;
  private List<Integer> cachedContainedClasses;

  private static ClassifierCache classifierCache = new ClassifierCache();

  private List<Set<String>> instancesCluster = null;

  protected MCTreeNode() {
    this.children = new LinkedList<>();
  }

  public MCTreeNode(final EMCNodeType nodeType, final String classifierID) throws Exception {
    this.children = new LinkedList<>();
    this.cachedContainedClasses = new LinkedList<>();
    this.nodeType = nodeType;
    this.classifierID = classifierID;

    Classifier baseClassifier;
    baseClassifier = AbstractClassifier.forName(classifierID, null);
    switch (nodeType) {
      case ONEVSREST: {
        MultiClassClassifier mcc = new MultiClassClassifier();
        mcc.setClassifier(baseClassifier);
        this.classifier = mcc;
        break;
      }
      case ALLPAIRS: {
        MultiClassClassifier mcc = new MultiClassClassifier();
        try {
          mcc.setOptions(new String[] { "-M", "" + 3 });
        } catch (Exception e) {
          e.printStackTrace();
        }
        mcc.setClassifier(baseClassifier);
        this.classifier = mcc;
        break;
      }
      case DIRECT:
        this.classifier = baseClassifier;
        break;
      default:
        break;
    }
  }

  public EMCNodeType getNodeType() {
    return this.nodeType;
  }

  public void addChild(final MCTreeNode newNode) {
    if (newNode.getNodeType() == EMCNodeType.MERGE) {
      for (MCTreeNode child : newNode.getChildren()) {
        this.children.add(child);
      }
    } else {
      this.children.add(newNode);
    }
  }

  public List<MCTreeNode> getChildren() {
    return this.children;
  }

  public List<Integer> getContainedClasses() {
    if (this.cachedContainedClasses.isEmpty()) {
      for (MCTreeNode child : this.children) {
        this.cachedContainedClasses.addAll(child.getContainedClasses());
      }
    }
    return this.cachedContainedClasses;
  }

  @Override
  public void buildClassifier(final Instances data) throws Exception {
    assert (this.getNodeType() != EMCNodeType.MERGE) : "MERGE node detected while building classifier. This must not happen!";

    // sort class split into clusters
    this.instancesCluster = new LinkedList<>();
    IntStream.range(0, this.children.size()).forEach(x -> this.instancesCluster.add(new HashSet<>()));
    for (MCTreeNode child : this.children) {
      for (Integer classIndex : child.getContainedClasses()) {
        this.instancesCluster.get(this.children.indexOf(child)).add(data.classAttribute().value(classIndex));
      }
    }

    // refactor training data with respect to the split clusters and build the classifier
    this.trainingData = WekaUtil.mergeClassesOfInstances(data, this.instancesCluster);

    Classifier cachedClassifier = classifierCache.getCachedClassifier(this.classifierID, this.getNodeType(), this.trainingData);
    cachedClassifier = null;
    if (cachedClassifier != null) {
      this.classifier = cachedClassifier;
      this.fetchedFromCache = true;
    } else {
      this.classifier.buildClassifier(this.trainingData);
      classifierCache.cacheClassifier(this.classifierID, this.getNodeType(), this.trainingData, this.classifier);
      this.fetchedFromCache = false;
    }

    // recursively build classifiers for children
    this.children.stream().parallel().forEach(child -> {
      try {
        child.buildClassifier(data);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  private Instances trainingData;
  private boolean fetchedFromCache;

  @Override
  public double classifyInstance(final Instance instance) throws Exception {
    Instance iNew = WekaUtil.getRefactoredInstance(instance, IntStream.range(0, this.children.size()).mapToObj(x -> x + ".0").collect(Collectors.toList()));
    int childIndex = (int) this.classifier.classifyInstance(iNew);
    try {
      double classification = this.children.get(childIndex).classifyInstance(iNew);
      return classification;
    } catch (IndexOutOfBoundsException e) {
      // XXX sysout sysexit
      System.out.println(Thread.currentThread().getName() + ": " + this.children);
      System.out.println(Thread.currentThread().getName() + ": " + iNew);
      System.out.println(Thread.currentThread().getName() + ": Predicted childIndex: " + this.classifier.classifyInstance(iNew));
      System.out.println(Thread.currentThread().getName() + ": " + this.classifierID + " " + this.nodeType);
      System.out.println(Thread.currentThread().getName() + ": Fetched from cache " + this.fetchedFromCache);
      System.exit(0);
    }
    return -1;
  }

  @Override
  public double[] distributionForInstance(final Instance instance) throws Exception {
    try {
      double[] distribution = this.classifier.distributionForInstance(instance);
      return distribution;
    } catch (ArrayIndexOutOfBoundsException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Capabilities getCapabilities() {
    return this.classifier.getCapabilities();
  }

  @Override
  public int getHeight() {
    return 1 + this.children.stream().map(x -> x.getHeight()).mapToInt(x -> (int) x).max().getAsInt();
  }

  @Override
  public int getDepthOfFirstCommonParent(final List<Integer> classes) {
    for (MCTreeNode child : this.children) {
      if (child.getContainedClasses().containsAll(classes)) {
        return 1 + child.getDepthOfFirstCommonParent(classes);
      }
    }
    return 1;
  }

  public static void clearCache() {
    classifierCache.clear();
  }

  public static ClassifierCache getClassifierCache() {
    return classifierCache;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("(");
    sb.append(this.classifierID);
    sb.append(":");
    sb.append(this.nodeType);
    sb.append(")");

    sb.append("{");

    boolean first = true;
    for (MCTreeNode child : this.children) {
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

}
