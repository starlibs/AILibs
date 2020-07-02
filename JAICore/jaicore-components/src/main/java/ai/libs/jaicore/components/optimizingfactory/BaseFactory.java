package ai.libs.jaicore.components.optimizingfactory;

import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.ComponentInstance;

public interface BaseFactory<T> {
	public T getComponentInstantiation(ComponentInstance groundComponent) throws ComponentInstantiationFailedException;
}
