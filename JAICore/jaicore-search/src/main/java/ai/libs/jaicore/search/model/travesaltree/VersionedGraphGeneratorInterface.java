/**
 * 
 */
package ai.libs.jaicore.search.model.travesaltree;

import ai.libs.jaicore.search.core.interfaces.GraphGenerator;

/**
 * A Graphgenerator which is extended by versioning
 * @author jkoepe
 *
 */
public interface VersionedGraphGeneratorInterface<T, A> extends GraphGenerator<T, A> {
	
	public void setNodeNumbering(boolean numbering);	
	
	
}
