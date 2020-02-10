package ai.libs.hyperopt.builder;

import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.hyperopt.EHyperoptOptimizer;

public interface IHyperoptBuilder {

	public IHyperoptBuilder withComponents(ComponentLoader loader);

	public IHyperoptBuilder withOptimizer(EHyperoptOptimizer optimizer);

}
