package ai.libs.jaicore.ml.weka.learner;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import weka.classifiers.Classifier;

public interface IWekaClassifier extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> {

	public Classifier getClassifier();

}
