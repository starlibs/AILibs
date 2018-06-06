package hasco.query;

import hasco.model.ComponentInstance;

public interface Factory<T> {
	public T getComponentInstantiation(ComponentInstance groundComponent) throws Exception;
}
