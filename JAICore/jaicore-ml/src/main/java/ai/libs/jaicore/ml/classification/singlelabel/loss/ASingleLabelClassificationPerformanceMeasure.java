package ai.libs.jaicore.ml.classification.singlelabel.loss;

import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicHomogeneousPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.core.evaluation.loss.APredictionPerformanceMeasure;

public abstract class ASingleLabelClassificationPerformanceMeasure extends APredictionPerformanceMeasure<Object, Object> implements IDeterministicHomogeneousPredictionPerformanceMeasure<Object> {

}
