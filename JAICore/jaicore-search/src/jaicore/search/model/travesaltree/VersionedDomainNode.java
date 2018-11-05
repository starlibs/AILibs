package jaicore.search.model.travesaltree;

/**
 * Wrapper class which adds an ID to the node
 * @author jkoepe
 *
 * @param <T>
 */
public class VersionedDomainNode<T> {

	//variables - only the node and the id of the node
	private T node;
	private int id;
	
	public VersionedDomainNode(T node) {
		this.node = node;
		this.id = 0;
	}
	
	public VersionedDomainNode(T node, int id) {
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
		if (id == 0)
			this.id = id;
	}

	
		
	
	
}
