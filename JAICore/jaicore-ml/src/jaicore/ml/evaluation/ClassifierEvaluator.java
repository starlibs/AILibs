package jaicore.ml.evaluation;

import jaicore.basic.IObjectEvaluator;
import weka.classifiers.Classifier;
/**
 * Can perform a (cross)-validation process on a {@link Classifier}.
 *
 */
public interface ClassifierEvaluator extends IObjectEvaluator<Classifier, Double> {
}
