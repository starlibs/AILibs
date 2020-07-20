package ai.libs.jaicore.ml.regression.loss.dataset;

import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

import ai.libs.jaicore.ml.classification.loss.dataset.APredictionPerformanceMeasure;

public abstract class ARegressionMeasure extends APredictionPerformanceMeasure<Double, IRegressionPrediction> implements IDeterministicPredictionPerformanceMeasure<Double, IRegressionPrediction> {

}
