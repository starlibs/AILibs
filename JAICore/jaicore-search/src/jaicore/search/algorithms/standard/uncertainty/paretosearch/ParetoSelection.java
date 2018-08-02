package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
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
		return this.open.addAll(c);
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

	/**
	 * Calculates the pareto front from the open list.
	 * @return Pareto front.
	 */
	private LinkedHashSet<Node<T, V>> calcParetoFront() {
		LinkedHashSet<Node<T, V>> paretoFront = new LinkedHashSet<>();

		// TODO: improve this algorithm, since runtime is quadratic atm.
		for (Node<T, V> n : this.open) {
			if (!isDominated(n)) {
				paretoFront.add(n);
			}
		}

		return paretoFront;
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
	/**
	 * Return a node from pareto front.
	 */
	public Node<T, V> peek() {
		// Calc pareto front.
		LinkedHashSet<Node<T, V>> paretoFront = this.calcParetoFront();

		// Pick element at random.
		Random rand = new Random();
		int index = rand.nextInt(paretoFront.size());
		Iterator<Node<T, V>> iter = paretoFront.iterator();
		for (int i = 0; i < index; i++) {
			iter.next();
		}
		return iter.next();
	}

	@Override
	public boolean remove(Object o) {
		return open.remove(o);
	}

}
