package jaicore.ml;

import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class RandomUniformClassifier implements Classifier {

  @Override
  public void buildClassifier(final Instances data) throws Exception {

  }

  @Override
  public double classifyInstance(final Instance instance) throws Exception {
    return 0;
  }

  @Override
  public double[] distributionForInstance(final Instance instance) throws Exception {
    double[] distribution = new double[instance.numClasses()];
    for (int i = 0; i < distribution.length; i++) {
      distribution[i] = (double) 1 / instance.numClasses();
    }
    return distribution;
  }

  @Override
  public Capabilities getCapabilities() {
    return null;
  }

}
