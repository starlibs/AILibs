package ai.libs.jaicore.components.optimizingfactory;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.algorithm.AOptimizer;
import ai.libs.jaicore.components.api.IEvaluatedSoftwareConfigurationSolution;
import ai.libs.jaicore.components.model.SoftwareConfigurationProblem;

public abstract class SoftwareConfigurationAlgorithm<P extends SoftwareConfigurationProblem<V>, O extends IEvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>> extends AOptimizer<P, O, V> {

	protected SoftwareConfigurationAlgorithm(final P input) {
		super(input);
	}

	protected SoftwareConfigurationAlgorithm(final IOwnerBasedAlgorithmConfig config, final P input) {
		super(config, input);
	}

}
