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
		// Only add if the node isnt dominated
		System.out.println("Add Node " + n);
		if (visualize) {
			if (n.getInternalLabel() instanceof Double) {
				visualizer.update((Double)n.getInternalLabel(), (Double) n.getAnnotation("uncertainty"));
			}
		}
		if (isDominated(n)) {
			return false;
		} else {
			return open.add(n);
		}
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
		boolean changed = false; // have to return if the collection has changed as a result of this call
		for (Node<T, V> n : c) {
			if (!isDominated(n)) {
				changed |= this.open.add(n);
			}
		}
		return changed;
	}

	/**
	 * Checks if a node is dominated by any other node contained in the pareto set.
	 * Atm, this is harcoded for "f" and "uncertainty" of a node.
	 * A node p is dominated iff there exist a point q in the pareto set, such that
	 * (q.f <= p.f and q.uncertainty < p.uncertainty) or (q.f < p.f and q.uncertainty <= p.uncertainty)
	 * @param p The node to check.
	 */
	private boolean isDominated(Node<T, V> p) {
		for (Node<T,V> q : this.open) {
			V q_f = (V) q.getAnnotation("f");
			V p_f = (V) p.getAnnotation("f");

			double q_uncertainty = (double) q.getAnnotation("uncertainty");
			double p_uncertainty = (double) p.getAnnotation("uncertainty");

			if (((q_f.compareTo(p_f) < 0) && (q_uncertainty <= p_uncertainty)) || ((q_f.compareTo(p_f) <= 0) && (q_uncertainty < p_uncertainty))) {
				return true;
			}

		}
		return false;
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
