package ai.libs.jaicore.ml.weka.learner;

import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassifier;

import weka.classifiers.Classifier;

public interface IWekaClassifier extends ISingleLabelClassifier {

	public Classifier getClassifier();

}
