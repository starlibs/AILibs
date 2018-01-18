package jaicore.search.structure.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

public class PriorityQueueOpen<E> implements OpenCollection<E> {

	private PriorityBlockingQueue<E> open;
	
	public PriorityQueueOpen() {
		this.open = new PriorityBlockingQueue<E>();
	}
	
	@Override
	public boolean add(E e) {
		return open.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return open.addAll(c);
	}

	@Override
	public void clear() {
		open.clear();
	}

	@Override
	public boolean contains(Object o) {
		return open.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return open.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return open.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return open.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return open.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return open.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return open.retainAll(c);
	}

	@Override
	public int size() {
		return open.size();
	}

	@Override
	public Object[] toArray() {
		return open.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return open.toArray(a);
	}

	@Override
	public E next() {
		return open.poll();
	}

	@Override
	public E peek() {
		return open.peek();
	}

}
