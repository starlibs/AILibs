package jaicore.search.structure.core;

/**
 * Wrapper class which adds an ID to the node
 * @author jkoepe
 *
 * @param <T>
 */
public class VersionedT<T> {

	//variables - only the node and the id of the node
	private T node;
	private int id;
	
	public VersionedT(T node) {
		this.node = node;
		this.id = 0;
	}
	
	public VersionedT(T node, int id) {
		this.node = node;
		this.id = id;
	}

	/**
	 * @return the node
	 */
	public T getNode() {
		return node;
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(T node) {
		this.node = node;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	
		
	
	
}
