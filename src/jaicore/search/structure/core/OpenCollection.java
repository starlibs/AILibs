/**
 * 
 */
package jaicore.search.structure.core;

import java.util.Collection;

/**
 * @author joern_000
 *
 */
public interface OpenCollection<E> extends Collection<E> {
	
	public E next();
	
	//needed ?
	public E peek();
}
