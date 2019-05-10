package jaicore.ml.evaluation.evaluators.weka.factory;

import jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;
import weka.core.Instances;

public interface IClassifierEvaluatorFactory {

	public IClassifierEvaluator getIClassifierEvaluator(Instances dataset, long seed) throws ClassifierEvaluatorConstructionFailedException;

}
