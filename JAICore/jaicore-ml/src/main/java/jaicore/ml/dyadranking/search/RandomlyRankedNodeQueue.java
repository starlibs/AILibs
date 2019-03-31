package jaicore.ml.dyadranking.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.model.travesaltree.Node;

/**
 * A node queue for the best first search that inserts new nodes at a random
 * position in the list.
 * 
 * @author Helena Graf
 *
 * @param <N>
 * @param <V>
 */
public class RandomlyRankedNodeQueue<N, V extends Comparable<V>> implements Queue<Node<N, V>> {

	private Random random;
	private Logger logger = LoggerFactory.getLogger(RandomlyRankedNodeQueue.class);

	public RandomlyRankedNodeQueue(int seed) {
		this.random = new Random(seed);
	}

	/** the actual queue of nodes */
	private List<Node<N, V>> openList = new ArrayList<>();

	@Override
	public int size() {
		return openList.size();
	}

	@Override
	public boolean isEmpty() {
		return openList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return openList.contains(o);
	}

	@Override
	public Iterator<Node<N, V>> iterator() {
		return openList.iterator();
	}

	@Override
	public Object[] toArray() {
		return openList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return openList.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return openList.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return openList.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Node<N, V>> c) {
		boolean changed = false;
		for (Node<N, V> node : c) {
			if (this.add(node)) {
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		openList.clear();
	}

	@Override
	public boolean add(Node<N, V> e) {
		int position = random.nextInt(openList.size() + 1);
		logger.debug("Add node at random position {} to OPEN list of size {}.", position, openList.size());
		openList.add(position, e);
		return true;
	}

	@Override
	public boolean offer(Node<N, V> e) {
		this.add(e);
		return true;
	}

	@Override
	public Node<N, V> remove() {
		return openList.remove(0);
	}

	@Override
	public Node<N, V> poll() {
		if (!this.isEmpty()) {
			return this.remove();
		} else {
			return null;
		}
	}

	@Override
	public Node<N, V> element() {
		return openList.get(0);
	}

	@Override
	public Node<N, V> peek() {
		if (!this.isEmpty()) {
			return this.element();
		} else {
			return null;
		}
	}

}
