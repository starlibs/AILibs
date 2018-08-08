package jaicore.graph;

import jaicore.basic.ILoggingCustomizable;

/**
 * 
 * @author fmohr
 *
 * @param <V> Class for nodes
 * @param <E> Class for edges
 */
public interface IObservableGraphAlgorithm<V,E> {
	public void registerListener(Object listener);
}
