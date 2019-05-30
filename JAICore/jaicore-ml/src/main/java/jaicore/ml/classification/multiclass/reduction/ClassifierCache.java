package jaicore.ml.classification.multiclass.reduction;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import jaicore.basic.sets.SetUtil.Pair;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class ClassifierCache extends HashMap<Integer, Pair<Classifier, Instances>> {

  /**
   *
   */
  private static final long serialVersionUID = -8463580964568016772L;

  private Lock cacheLock = new ReentrantLock();

  public ClassifierCache() {

  }

  public Classifier getCachedClassifier(final String classifierName, final EMCNodeType classificationStrategy, final Instances data) {
    int hashCode = new HashCodeBuilder().append(classifierName).append(classificationStrategy).append(data.toString()).toHashCode();
    Pair<Classifier, Instances> pair = this.get(hashCode);
    if (pair == null) {
      return null;
    } else {
      return this.get(hashCode).getX();
    }
  }

  public void cacheClassifier(final String classifierName, final EMCNodeType classificationStrategy, final Instances data, final Classifier classifier) {
    // int hashCode = new
    // HashCodeBuilder().append(classifierName).append(classificationStrategy).append(data.toString()).toHashCode();
    // this.cacheLock.lock();
    // try {
    // this.put(hashCode, new Pair<>(classifier, data));
    // } finally {
    // this.cacheLock.unlock();
    // }
  }

  public Instances getCachedTrainingData(final String classifierName, final EMCNodeType classificationStrategy, final Instances data) {
    int hashCode = new HashCodeBuilder().append(classifierName).append(classificationStrategy).append(data.toString()).toHashCode();
    Pair<Classifier, Instances> pair = this.get(hashCode);
    if (pair == null) {
      return null;
    } else {
      return this.get(hashCode).getY();
    }
  }

}
