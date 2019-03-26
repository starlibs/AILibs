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

public class RandomlyRankedNodeQueue<N, V extends Comparable<V>> implements Queue<Node<N, V>> {

	private Random random;
	private Logger logger = LoggerFactory.getLogger(RandomlyRankedNodeQueue.class);

	public RandomlyRankedNodeQueue(int seed) {
		this.random = new Random(seed);
	}

	/** the actual queue of nodes */
	private List<Node<N, V>> queue = new ArrayList<>();

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
		return queue.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return queue.containsAll(c);
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
		// TODO: implement
		return false;
	}

	@Override
	public void clear() {
		queue.clear();
	}

	@Override
	public boolean add(Node<N, V> e) {
		int position = random.nextInt(queue.size() + 1);
		logger.debug("Add node at random position {} to OPEN list of size {}.", position, queue.size());
		queue.add(position, e);
		return true;
	}

	@Override
	public boolean offer(Node<N, V> e) {
		this.add(e);
		return true;
	}

	@Override
	public Node<N, V> remove() {
		return queue.remove(0);
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
		return queue.get(0);
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
