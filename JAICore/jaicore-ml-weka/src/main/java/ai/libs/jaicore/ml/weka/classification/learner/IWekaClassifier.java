package ai.libs.jaicore.ml.weka.classification.learner;

import java.io.Serializable;

import org.api4.java.ai.ml.classification.IClassifier;

import weka.classifiers.Classifier;

public interface IWekaClassifier extends IClassifier, Serializable {

	public Classifier getClassifier();

}
