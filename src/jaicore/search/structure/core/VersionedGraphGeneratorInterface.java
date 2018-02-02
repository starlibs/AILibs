/**
 * 
 */
package jaicore.search.structure.core;


import java.util.Random;
/**
 * A Graphgenerator which is extended by versioning
 * @author jkoepe
 *
 */
public interface VersionedGraphGeneratorInterface<T, A> extends GraphGenerator<T, A> {
	
	public void setNodeNumbering(boolean numbering);	
	
	
}
