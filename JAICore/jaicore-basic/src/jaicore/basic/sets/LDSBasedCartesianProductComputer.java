package jaicore.basic.sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;

/**
 * This algorithms allows to compute an ordered Cartesian product. It is ordered
 * in the sense that it interprets the sets over which the product is built as
 * ORDERED sets and first generates tuples with items that appear first in the
 * sets.
 * 
 * The algorithm also works for ordinary unordered sets but is a bit slower why
 * another algorithm could be favorable.
 * 
 * @author fmohr
 *
 * @param <T>
 */
public class LDSBasedCartesianProductComputer<T> extends AAlgorithm<List<? extends Collection<T>>, List<List<T>>> {

	private class Node {
		final int nextDecision;
		final int defficiency;
		final List<T> tuple;

		public Node(int nextDecision, int defficiency, List<T> tuple) {
			super();
			this.nextDecision = nextDecision;
			this.defficiency = defficiency;
			this.tuple = tuple;
		}
	}

	private Queue<Node> open = new PriorityQueue<>((n1, n2) -> n1.defficiency - n2.defficiency);

	public LDSBasedCartesianProductComputer(List<? extends Collection<T>> collections) {
		super(collections);
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		switch (getState()) {
		case created: {
			open.add(new Node(0, 0, new ArrayList<>()));
			return activate();
		}
		case active: {
			checkTermination();
			if (open.isEmpty())
				return terminate();

			/* determine next cheapest path to a leaf */
			Node next;
			while ((next = open.poll()).nextDecision < getInput().size()) {
				int i = 0;
				for (T item : getInput().get(next.nextDecision)) {
					checkTermination();
					List<T> tuple = new ArrayList<>(next.tuple);
					tuple.add(item);
					open.add(new Node(next.nextDecision + 1, next.defficiency + i++, tuple));
				}
			}

			/* at this point, next should contain a fully specified tuple */
			assert next.tuple.size() == getInput().size();
			return new TupleOfCartesianProductFoundEvent<>(next.tuple);
		}
		default:
			throw new IllegalStateException();
		}
	}

	@SuppressWarnings("unchecked")
	public List<T> nextTuple() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		while (hasNext()) {
			AlgorithmEvent e = nextWithException();
			if (e instanceof AlgorithmFinishedEvent)
				return null;
			else if (e instanceof TupleOfCartesianProductFoundEvent)
				return ((TupleOfCartesianProductFoundEvent<T>) e).getTuple();
			else if (!(e instanceof AlgorithmInitializedEvent))
				throw new IllegalStateException("Cannot handle event of type " + e.getClass());
		}
		throw new IllegalStateException("No more elements but no AlgorithmFinishedEvent was generated!");
	}

	@Override
	public List<List<T>> call() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		List<List<T>> product = new ArrayList<>();
		List<T> nextTuple;
		while ((nextTuple = nextTuple()) != null) {
			product.add(nextTuple);
		}
		return product;
	}

}
