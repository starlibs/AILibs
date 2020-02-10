package ai.libs.hyperopt.builder;

import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.hyperopt.EHyperoptOptimizer;

public class HyperoptBuilder implements IHyperoptBuilder {

	private ComponentLoader componentLoader;
	private EHyperoptOptimizer optimizer;

	@Override
	public HyperoptBuilder withComponents(final ComponentLoader componentLoader) {
		this.componentLoader = componentLoader;
		return this;
	}

	@Override
	public IHyperoptBuilder withOptimizer(final EHyperoptOptimizer optimizer) {
		this.optimizer = optimizer;
		return this;
	}

}
