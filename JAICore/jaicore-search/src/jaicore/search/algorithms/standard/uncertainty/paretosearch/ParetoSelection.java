package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

import jaicore.search.algorithms.standard.uncertainty.UncertaintyFMeasure;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.OpenCollection;

public class ParetoSelection <N> implements OpenCollection<Node<N, UncertaintyFMeasure>> {
	
	private PriorityBlockingQueue<Node<N, UncertaintyFMeasure>> open;
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
	public boolean add(Node<N, UncertaintyFMeasure> n) {
		if (visualize) {
			UncertaintyFMeasure measure = n.getInternalLabel();
			visualizer.update(measure);
		}
		return open.add(n);
	}

	@Override
	public boolean addAll(Collection<? extends Node<N, UncertaintyFMeasure>> c) {
		if (visualize) {
			for(Node<N, UncertaintyFMeasure> n: c) {
				UncertaintyFMeasure measure = n.getInternalLabel();
				visualizer.update(measure);
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
	public Iterator<Node<N, UncertaintyFMeasure>> iterator() {
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
	public <T> T[] toArray(T[] a) {
		return open.toArray(a);
	}

	@Override
	public Node<N, UncertaintyFMeasure> peek() {
		return open.peek();
	}

	@Override
	public boolean remove(Object o) {
		return open.remove(o);
	}

}
