package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.OpenCollection;

public class ParetoSelection <T, V extends Comparable<V>> implements OpenCollection<Node<T, V>> {
	
	private PriorityBlockingQueue<Node<T, V>> open;
	boolean visualize;
	ParetoFrontVisualizer visualizer;
	
	public ParetoSelection(boolean visualizeFront) {
		open = new PriorityBlockingQueue<>();
		visualize = visualizeFront;
		if (visualizeFront) {
			visualizer = new ParetoFrontVisualizer();
			visualizer.show();
		}
	}
	
	@Override
	public boolean add(Node<T, V> n) {
		if (visualize) {
			if (n.getInternalLabel() instanceof Double) {
				visualizer.update((Double)n.getInternalLabel(), (Double) n.getAnnotation("uncertainty"));
			}
		}
		return open.add(n);
	}

	@Override
	public boolean addAll(Collection<? extends Node<T, V>> c) {
		if (visualize) {
			for(Node<T, V> n: c) {
				if (n.getInternalLabel() instanceof Double) {
					visualizer.update((Double)n.getInternalLabel(), (Double) n.getAnnotation("uncertainty"));
				}
			}
		}
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
	public Iterator<Node<T, V>> iterator() {
		return open.iterator();
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
	public <X> X[] toArray(X[] a) {
		return open.toArray(a);
	}

	@Override
	public Node<T, V> peek() {
		return open.peek();
	}

	@Override
	public boolean remove(Object o) {
		return open.remove(o);
	}

}
