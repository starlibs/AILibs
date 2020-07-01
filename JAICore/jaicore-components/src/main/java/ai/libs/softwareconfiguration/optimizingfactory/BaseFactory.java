package ai.libs.softwareconfiguration.optimizingfactory;

import ai.libs.softwareconfiguration.exceptions.ComponentInstantiationFailedException;
import ai.libs.softwareconfiguration.model.ComponentInstance;

public interface BaseFactory<T> {
	public T getComponentInstantiation(ComponentInstance groundComponent) throws ComponentInstantiationFailedException;
}
