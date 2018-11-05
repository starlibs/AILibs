package hasco.optimizingfactory;

import hasco.model.ComponentInstance;

public interface BaseFactory<T> {
	public T getComponentInstantiation(ComponentInstance groundComponent) throws Exception;
}
