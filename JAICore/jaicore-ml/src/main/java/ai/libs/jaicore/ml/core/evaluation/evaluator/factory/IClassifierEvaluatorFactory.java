package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import ai.libs.jaicore.ml.core.evaluation.evaluator.IClassifierEvaluator;

public interface IClassifierEvaluatorFactory<D> {

	public IClassifierEvaluator getIClassifierEvaluator(D dataset, long seed) throws ClassifierEvaluatorConstructionFailedException;

}
