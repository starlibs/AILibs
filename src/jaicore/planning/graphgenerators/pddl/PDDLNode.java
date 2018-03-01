package jaicore.planning.graphgenerators.pddl;

import fr.uga.pddl4j.planners.hsp.Node;
import jaicore.search.structure.core.AbstractNode;

public class PDDLNode extends AbstractNode{
	Node node;
	
	
	public PDDLNode(Node node) {
		this.node = node;
	}

	public Node getNode() {
		return this.node;
	}
}
