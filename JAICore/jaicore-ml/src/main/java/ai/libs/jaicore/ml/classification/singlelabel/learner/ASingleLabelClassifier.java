package ai.libs.jaicore.ml.classification.singlelabel.learner;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationDataset;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;

import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;

public abstract class ASingleLabelClassifier extends ASupervisedLearner<ISingleLabelClassificationInstance, ISingleLabelClassificationDataset> {

}
