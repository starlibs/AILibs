package ai.libs.hasco.optimizingfactory;

import ai.libs.hasco.core.SoftwareConfigurationProblem;
import ai.libs.hasco.model.EvaluatedSoftwareConfigurationSolution;
import ai.libs.jaicore.basic.algorithm.AOptimizer;
import ai.libs.jaicore.basic.algorithm.IAlgorithmConfig;

public abstract class SoftwareConfigurationAlgorithm<P extends SoftwareConfigurationProblem<V>, O extends EvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>> extends AOptimizer<P, O, V> {

	protected SoftwareConfigurationAlgorithm(final P input) {
		super(input);
	}

	protected SoftwareConfigurationAlgorithm(final IAlgorithmConfig config, final P input) {
		super(config, input);
	}

}
