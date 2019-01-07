package jaicore.search.structure.graphgenerator;

import jaicore.search.model.travesaltree.NodeExpansionDescription;

public interface SingleSuccessorGenerator<T,A> extends SuccessorGenerator<T, A> {

	/**
	 * generate the (i%N)-th ungenerated successor of the given node where N is the number of existing successors that have not been generated before.
	 * 
	 * returns null if no more successors exist.
	 * 
	 * @param i
	 * @return
	 */
	public NodeExpansionDescription<T,A> generateSuccessor(T node, int i) throws InterruptedException;
	
	public boolean allSuccessorsComputed(T node);
}
