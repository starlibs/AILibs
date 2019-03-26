package hasco.optimizingfactory;

import hasco.core.SoftwareConfigurationProblem;

public class OptimizingFactoryProblem<P extends SoftwareConfigurationProblem<V>, T, V extends Comparable<V>> {
	private final BaseFactory<T> baseFactory;
	private final P configurationProblem;

	public OptimizingFactoryProblem(BaseFactory<T> baseFactory, P configurationProblem) {
		super();
		this.baseFactory = baseFactory;
		this.configurationProblem = configurationProblem;
	}

	public BaseFactory<T> getBaseFactory() {
		return baseFactory;
	}

	public P getConfigurationProblem() {
		return configurationProblem;
	}
}
