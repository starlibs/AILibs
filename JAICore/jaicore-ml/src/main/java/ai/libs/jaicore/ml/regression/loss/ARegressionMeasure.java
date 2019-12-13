package ai.libs.jaicore.ml.regression.loss;

import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicHomogeneousPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.core.evaluation.loss.APredictionPerformanceMeasure;

public abstract class ARegressionMeasure extends APredictionPerformanceMeasure<Double, Double> implements IDeterministicHomogeneousPredictionPerformanceMeasure<Double> {

}
