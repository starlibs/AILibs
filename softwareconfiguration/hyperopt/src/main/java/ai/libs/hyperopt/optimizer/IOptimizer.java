package ai.libs.hyperopt.optimizer;

import ai.libs.hyperopt.OptimizationException;

/**
 *
 * @author kadirayk
 *
 */
public interface IOptimizer {

	public void optimize(String componentName) throws OptimizationException;

}
