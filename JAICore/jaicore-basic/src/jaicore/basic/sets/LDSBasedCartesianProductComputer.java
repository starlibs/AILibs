package jaicore.basic.sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;

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
	public AlgorithmEvent nextWithException() throws Exception {
		switch (getState()) {
		case created: {
			open.add(new Node(0, 0, new ArrayList<>()));
			setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		}
		case active: {
			if (open.isEmpty())
				return terminate();
			
			/* determine next cheapest path to a leaf */
			Node next;
			while ((next = open.poll()).nextDecision < getInput().size()) {
				int i = 0;
				for (T item : getInput().get(next.nextDecision)) {
					List<T> tuple = new ArrayList<>(next.tuple);
					tuple.add(item);
					open.add(new Node(next.nextDecision + 1, next.defficiency + i++, tuple));
				}
			}
			
			/* at this point, next should contain a fully specified tuple */
			System.out.println(next.tuple + ": " + next.defficiency);
			return new TupleOfCartesianProductFoundEvent<>(next.tuple);
		}
		default:
			throw new IllegalStateException();
		}
	}

	@Override
	public List<List<T>> call() {
		List<List<T>> product = new ArrayList<>();
		next(); // initialize
		while (hasNext()) {
			AlgorithmEvent e = next();
			if (e instanceof AlgorithmFinishedEvent)
				return product;
			else if (e instanceof TupleOfCartesianProductFoundEvent)
				product.add(((TupleOfCartesianProductFoundEvent<T>) e).getTuple());
			else
				throw new IllegalStateException("Cannot handle event of type " + e.getClass());
		}
		throw new IllegalStateException("No more elements but no AlgorithmFinishedEvent was generated!");
	}

}
