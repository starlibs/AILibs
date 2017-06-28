package util.search.graphgenerator;

import java.util.List;

import util.search.core.Node;
import util.search.core.NodeExpansionDescription;

public interface SuccessorGenerator<T,A> {


	
	/**
	 * Generate the successors for a given node.
	 *
	 * @param node The node we want to expand.
	 * @return A list of possible next steps.
	 */
	public List<NodeExpansionDescription<T,A>> generateSuccessors(Node<T,?> node);


}
