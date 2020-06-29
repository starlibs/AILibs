package ai.libs.softwareconfiguration.optimizingfactory;

import ai.libs.softwareconfiguration.model.SoftwareConfigurationProblem;

public class OptimizingFactoryProblem<P extends SoftwareConfigurationProblem<V>, T, V extends Comparable<V>> {
	private final BaseFactory<T> baseFactory;
	private final P configurationProblem;

	public OptimizingFactoryProblem(final BaseFactory<T> baseFactory, final P configurationProblem) {
		super();
		this.baseFactory = baseFactory;
		this.configurationProblem = configurationProblem;
	}

	public BaseFactory<T> getBaseFactory() {
		return this.baseFactory;
	}

	public P getConfigurationProblem() {
		return this.configurationProblem;
	}
}
