package ai.libs.jaicore.ml.classification.singlelabel.loss;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.loss.ISingleLabelClassificationMeasure;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassification;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPredictionAndGroundTruthTable;

import ai.libs.jaicore.ml.core.evaluation.loss.AMeasure;

public abstract class ASingleLabelClassificationMeasure extends AMeasure<ISingleLabelClassification, ISingleLabelClassificationPredictionAndGroundTruthTable> implements ISingleLabelClassificationMeasure {

}
