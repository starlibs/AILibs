package jaicore.ml;

import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class MajorityClassifier implements Classifier {

  private int majorityClassIndex = 0;

  @Override
  public void buildClassifier(final Instances data) throws Exception {
    int[] classCounter = new int[data.numClasses()];

    for (Instance i : data) {
      classCounter[(int) i.classValue()]++;
    }

    int classWithHighestCounter = 0;
    for (int i = 1; i < classCounter.length; i++) {
      if (classCounter[i] > classCounter[classWithHighestCounter]) {
        classWithHighestCounter = i;
      }
    }

    this.majorityClassIndex = classWithHighestCounter;
  }

  @Override
  public double classifyInstance(final Instance instance) throws Exception {
    return this.majorityClassIndex;
  }

  @Override
  public double[] distributionForInstance(final Instance instance) throws Exception {
    double[] distribution = new double[instance.numClasses()];
    distribution[this.majorityClassIndex] = 1;
    return distribution;
  }

  @Override
  public Capabilities getCapabilities() {
    return null;
  }

}
