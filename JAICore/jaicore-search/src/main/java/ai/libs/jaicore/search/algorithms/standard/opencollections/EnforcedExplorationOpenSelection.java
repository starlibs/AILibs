package ai.libs.jaicore.search.algorithms.standard.opencollections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

/**
 * This OPEN selection allows to enforce that the search is restricted to be searched under a given node
 *
 * @author fmohr
 *
 * @param <N>
 * @param <V>
 * @param <W>
 */
public class EnforcedExplorationOpenSelection<N, A, V extends Comparable<V>> extends PriorityQueue<BackPointerPath<N, A, V>> {

	private static final Logger logger = LoggerFactory.getLogger(EnforcedExplorationOpenSelection.class);

	private final Collection<BackPointerPath<N, A, V>> suspended = new ArrayList<>();
	private BackPointerPath<N, A, V> temporaryRoot; // this is the node that is currently used as a root

	/**
	 * Set the temporary root under which the search should explore.
	 * This means to decide for each node on OPEN or SUSPENDED again whether they are under the new temporary root
	 *
	 * @param temporaryRoot
	 */
	public void setTemporaryRoot(final BackPointerPath<N, A, V> temporaryRoot) {
		int numItemsBefore = this.size() + this.suspended.size();
		this.temporaryRoot = temporaryRoot;
		Collection<BackPointerPath<N, A, V>> openAndSuspendedNodes = SetUtil.union(this.suspended, this);
		this.suspended.clear();
		this.clear();
		for (BackPointerPath<N, A, V> n : openAndSuspendedNodes) {
			BackPointerPath<N, A, V> current = n;
			boolean isSuspsended = true;
			while (current != null) {
				if (current.equals(temporaryRoot)) {
					isSuspsended = false;
					break;
				}
				current = current.getParent();
			}
			if (isSuspsended) {
				this.suspended.add(n);
			} else {
				this.add(n);
			}
		}
		int numItemsAfter = this.size() + this.suspended.size();
		assert numItemsAfter == numItemsBefore : "The total number of elements in OPEN/SUSPENDED has changed from " + numItemsBefore + " to " + numItemsAfter + " by setting the temporary root!";
	}

	public BackPointerPath<N, A, V> getTemporaryRoot() {
		return this.temporaryRoot;
	}
}
