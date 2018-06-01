/**
 * 
 */
package jaicore.search.structure.core;

/**
 * A Graphgenerator which is extended by versioning
 * @author jkoepe
 *
 */
public interface VersionedGraphGeneratorInterface<T, A> extends GraphGenerator<T, A> {
	
	public void setNodeNumbering(boolean numbering);	
	
	
}
