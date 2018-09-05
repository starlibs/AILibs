package jaicore.planning.graphgenerators.pddl;

import fr.uga.pddl4j.planners.hsp.Node;
import jaicore.search.model.travesaltree.AbstractNode;

public class PDDLNode extends AbstractNode{
	private Node node;
	private int id;
	
	
	public PDDLNode(Node node, int id) {
		this.id = id;
		this.node = node;
		
	}

	public Node getNode() {
		return this.node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PDDLNode other = (PDDLNode) obj;
		if (id != other.id)
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}


}
