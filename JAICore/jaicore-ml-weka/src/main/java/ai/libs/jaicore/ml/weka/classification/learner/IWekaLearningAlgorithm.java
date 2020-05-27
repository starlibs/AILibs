package ai.libs.jaicore.ml.weka.classification.learner;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;

public interface IWekaLearningAlgorithm extends IAlgorithm<ILabeledDataset<?>, IWekaClassifier> {

}
