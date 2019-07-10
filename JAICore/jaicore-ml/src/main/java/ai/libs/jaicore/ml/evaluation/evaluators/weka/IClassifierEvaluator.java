package ai.libs.jaicore.ml.evaluation.evaluators.weka;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import weka.classifiers.Classifier;

public interface IClassifierEvaluator extends IObjectEvaluator<Classifier, Double> {
}
