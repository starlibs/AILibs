package jaicore.search.algorithms.standard.opencollections;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil;
import jaicore.search.algorithms.standard.bestfirst.model.PriorityQueueOpen;
import jaicore.search.model.travesaltree.Node;

/**
 * This OPEN selection allows to enforce that the search is restricted to be searched under a given node
 * 
 * @author fmohr
 *
 * @param <N>
 * @param <V>
 * @param <W>
 */
public class EnforcedExplorationOpenSelection<N,V extends Comparable<V>> extends PriorityQueueOpen<Node<N,V>> {
	
	private static final Logger logger = LoggerFactory.getLogger(EnforcedExplorationOpenSelection.class);
	
	private final Collection<Node<N, V>> suspended = new ArrayList<>();
	private Node<N,V> temporaryRoot; // this is the node that is currently used as a root 
	
	/**
	 * Set the temporary root under which the search should explore.
	 * This means to decide for each node on OPEN or SUSPENDED again whether they are under the new temporary root
	 * 
	 * @param temporaryRoot
	 */
	public void setTemporaryRoot(Node<N,V> temporaryRoot) {
		this.temporaryRoot = temporaryRoot;
		Collection<Node<N,V>> openAndSuspendedNodes = SetUtil.union(suspended, this);
		suspended.clear();
		this.clear();
		for (Node<N,V> n : openAndSuspendedNodes) {
			Node<N,V> current = n;
			boolean isSuspsended = true;
			while (current != null) {
				if (current.equals(temporaryRoot)) {
					isSuspsended = false;
					break;
				}
				current = current.getParent();
			}
			if (isSuspsended)
				suspended.add(n);
			else
				this.add(n);
		}
	}

	public Node<N, V> getTemporaryRoot() {
		return temporaryRoot;
	}
}
