package ai.libs.softwareconfiguration.optimizingfactory;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.algorithm.AOptimizer;
import ai.libs.softwareconfiguration.model.EvaluatedSoftwareConfigurationSolution;
import ai.libs.softwareconfiguration.model.SoftwareConfigurationProblem;

public abstract class SoftwareConfigurationAlgorithm<P extends SoftwareConfigurationProblem<V>, O extends EvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>> extends AOptimizer<P, O, V> {

	protected SoftwareConfigurationAlgorithm(final P input) {
		super(input);
	}

	protected SoftwareConfigurationAlgorithm(final IOwnerBasedAlgorithmConfig config, final P input) {
		super(config, input);
	}

}
