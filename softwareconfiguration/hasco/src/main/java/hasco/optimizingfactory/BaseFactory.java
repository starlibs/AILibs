package hasco.optimizingfactory;

import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;

public interface BaseFactory<T> {
	public T getComponentInstantiation(ComponentInstance groundComponent) throws ComponentInstantiationFailedException;
}
