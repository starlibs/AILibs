package jaicore.ml.evaluation.evaluators.weka.factory;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;

public interface IClassifierEvaluatorFactory {

	public IClassifierEvaluator getIClassifierEvaluator(IDataset<? extends IInstance> dataset, long seed);

}
