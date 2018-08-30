/**
 * 
 */
package jaicore.search.algorithms.standard.bestfirst.model;

import java.util.Collection;

public interface OpenCollection<E> extends Collection<E> {
		
	/**
	 * Method to look up the next element of the collection without removing it
	 * @return
	 * 	The next element
	 */
	public E peek();
	
	
	public boolean remove(Object o);
}
