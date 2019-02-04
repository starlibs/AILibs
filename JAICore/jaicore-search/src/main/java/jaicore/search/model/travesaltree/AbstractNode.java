package jaicore.search.model.travesaltree;
/**
 * abstract class for nodeobjects which only contains a simple equals method and the id 
 * @author jkoepe
 *
 */
public abstract class AbstractNode {
	
	//the id of the node
	protected int id;
	
	public AbstractNode() {
		this.id = 0;
	}
	
	public AbstractNode(int id) {
		this.id = id;
	}
	
	
	/**
	 * Method to set the id, if it was not set in the construction
	 * @param id
	 * 		The id the node should get.
	 */
	public void setId(int id) {
		if(this.id == 0) {
			this.id = id;
		}
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
