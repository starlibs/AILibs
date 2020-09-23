package ai.libs.jaicore.components.optimizingfactory;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;

public interface BaseFactory<T> {
	public T getComponentInstantiation(IComponentInstance groundComponent) throws ComponentInstantiationFailedException;
}
