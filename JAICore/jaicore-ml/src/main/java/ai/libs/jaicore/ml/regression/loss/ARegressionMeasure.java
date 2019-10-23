package ai.libs.jaicore.ml.regression.loss;

import org.api4.java.ai.ml.core.evaluation.loss.IMeasure;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

import ai.libs.jaicore.ml.core.evaluation.loss.AMeasure;

public abstract class ARegressionMeasure extends AMeasure<IRegressionPrediction, IRegressionPredictionAndGroundTruthTable> implements IMeasure<IRegressionPrediction, IRegressionPredictionAndGroundTruthTable> {

}
