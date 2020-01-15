package ai.libs.hasco.optimizingfactory;

import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.ComponentInstance;

public interface BaseFactory<T> {
	public T getComponentInstantiation(ComponentInstance groundComponent) throws ComponentInstantiationFailedException;
}
