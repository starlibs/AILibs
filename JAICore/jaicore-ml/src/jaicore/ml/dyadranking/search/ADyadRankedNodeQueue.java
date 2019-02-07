package jaicore.ml.dyadranking.search;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.dyadranking.algorithm.ADyadRanker;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;
import jaicore.search.model.travesaltree.Node;

/**
 * A queue whose elements are nodes, sorted by a dyad ranker.
 * 
 * @author Helena Graf
 *
 * @param <N>
 *            First node parameter
 * @param <V>
 *            Second node parameter
 */
public abstract class ADyadRankedNodeQueue<N, V extends Comparable<V>> implements Queue<Node<N, V>> {

	/** the dyad ranker used to rank the nodes */
	//TODO: load a pre-trained PLNet 
	private ADyadRanker dyadRanker = new PLNetDyadRanker();

	/** the actual queue of nodes */
	private List<Node<N, V>> queue;

	/** characterizations of the nodes (not ordered the same way as the nodes!) */
	private List<Vector> nodeCharacterizations;

	/** characterization of the context the nodes are ranked in */
	private Vector contextCharacterization;

	/**
	 * the instance which is used repeatedly to query the dyadranker, which infers
	 * an ordering of the nodes
	 */
	private SparseDyadRankingInstance queryInstance = new SparseDyadRankingInstance(contextCharacterization,
			nodeCharacterizations);

	/** connects nodes to their respective characterizations */
	private BiMap<Node<N, V>, Vector> nodesAndCharacterizationsMap = HashBiMap.create();

	/**
	 * Constructs a new DyadRankedNodeQueue that ranks the nodes in the queue
	 * according to the given context characterization.
	 * 
	 * @param contextCharacterization
	 *            the characterization of the context the nodes are ranked in
	 */
	public ADyadRankedNodeQueue(Vector contextCharacterization) {
		this.contextCharacterization = contextCharacterization;
	}

	/**
	 * Constructs a new DyadRankedNodeQueue that ranks the nodes in the queue
	 * according to the given context characterization and given dyad ranker.
	 * 
	 * @param contextCharacterization
	 *            the characterization of the context the nodes are ranked in
	 * @param dyadRanker
	 *            the dyad ranker to be used for the ranking of the nodes
	 */
	public ADyadRankedNodeQueue(Vector contextCharacterization, ADyadRanker dyadRanker) {
		this(contextCharacterization);
		this.dyadRanker = dyadRanker;
	}

	/**
	 * Provide a characterization of the given node to be used by the dyad ranker.
	 * 
	 * @param node
	 *            the node to be characterized
	 * @return the characterization of the node
	 */
	protected abstract Vector characterize(Node<N, V> node);

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return queue.contains(o);
	}

	@Override
	public Iterator<Node<N, V>> iterator() {
		return queue.iterator();
	}

	@Override
	public Object[] toArray() {
		return queue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return queue.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Node<?, ?>) {
			int index = -1;
			for (int i = 0; i < queue.size(); i++) {
				if (queue.get(i).equals(o)) {
					index = i;
				}
			}

			if (index != -1) {
				removeNodeAtPosition(index);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return queue.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Node<N, V>> c) {
		boolean changed = false;

		for (Node<N, V> elem : c) {
			if (this.add(elem)) {
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;

		for (Object o : c) {
			if (this.remove(o)) {
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {
		queue.clear();
		nodesAndCharacterizationsMap.clear();
		nodeCharacterizations.clear();
	}

	@Override
	public boolean add(Node<N, V> e) {
		if (queue.contains(e)) {
			return true;
		} else if (e != null) {
			try {
				// characterize new node
				Vector characterization = characterize(e);
				nodeCharacterizations.add(characterization);

				// predict new ranking and reorder queue accordingly
				IDyadRankingInstance prediction = dyadRanker.predict(queryInstance);
				queue.clear();
				for (int i = 0; i < prediction.length(); i++) {
					queue.add(nodesAndCharacterizationsMap.inverse()
							.get(prediction.getDyadAtPosition(i).getAlternative()));
				}

				// add new pairing of node and characterization
				nodesAndCharacterizationsMap.put(e, characterization);

				return true;
			} catch (PredictionException e1) {
				e1.printStackTrace();
				// remove unneeded characterization (ranking has failed)
				nodeCharacterizations.remove(nodeCharacterizations.size() - 1);
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean offer(Node<N, V> e) {
		return this.add(e);
	}

	@Override
	public Node<N, V> remove() {
		return removeNodeAtPosition(0);
	}

	public Node<N, V> removeNodeAtPosition(int i) {
		Node<N, V> removedNode = queue.remove(i);
		nodeCharacterizations.remove(nodesAndCharacterizationsMap.get(removedNode));
		nodesAndCharacterizationsMap.remove(removedNode);
		return removedNode;
	}

	@Override
	public Node<N, V> poll() {
		if (!queue.isEmpty()) {
			return this.remove();
		} else {
			return null;
		}
	}

	@Override
	public Node<N, V> element() {
		return queue.get(0);
	}

	@Override
	public Node<N, V> peek() {
		if (!queue.isEmpty()) {
			return this.element();
		} else {
			return null;
		}
	}

	/**
	 * Get the dyad ranker used to rank the nodes.
	 * 
	 * @return the dyad ranker
	 */
	public ADyadRanker getDyadRanker() {
		return dyadRanker;
	}

	/**
	 * Set which dyad ranker shall be used to rank the nodes. It is not trained in
	 * this class, so it must be pre-trained before setting it as a dyad ranker for
	 * this queue!
	 * 
	 * @param dyadRanker
	 *            the dyad ranker
	 */
	public void setDyadRanker(ADyadRanker dyadRanker) {
		this.dyadRanker = dyadRanker;
	}

}
