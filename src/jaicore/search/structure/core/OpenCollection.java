/**
 * 
 */
package jaicore.search.structure.core;

import java.util.Collection;

/**
 * @author jkoepe
 *
 */
public interface OpenCollection<E> extends Collection<E> {
	/**
	 * Returns the next object of the collection
	 * @return
	 * 		The enxt object
	 */
	public E next();
	
	/**
	 * Method to look up the next element of the collection without removing it
	 * @return
	 * 	The next element
	 */
	public E peek();
}
