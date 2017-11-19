package jaicore.search.structure.graphgenerator;

import java.util.Collection;

import jaicore.search.structure.core.NodeExpansionDescription;

public interface SuccessorGenerator<T,A> {


	
	/**
	 * Generate the successors for a given node.
	 *
	 * @param node The node we want to expand.
	 * @return A list of possible next steps.
	 */
<<<<<<< HEAD
	public List<NodeExpansionDescription<T,A>> generateSuccessors(T node);
=======
	public Collection<NodeExpansionDescription<T,A>> generateSuccessors(T node);
>>>>>>> fd1c7dd64da900832ded0b2d0dc43f9cb47f6214


}
