package jaicore.basic.sets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.omg.CORBA.DefinitionKind;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.exceptions.DelayedCancellationCheckException;
import jaicore.basic.algorithm.exceptions.DelayedTimeoutCheckException;

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
public class LDSRelationComputer<T> extends AAlgorithm<RelationComputationProblem<T>, List<Object[]>> {

	private static class Node {
		Node parent;
		int nextDecision;
		int defficiency;
		Object val;

		public Node() {
			
		}
		public Node(Node parent, int nextDecision, int defficiency, Object val) {
			long start = System.currentTimeMillis();
			this.parent = parent;
			this.nextDecision = nextDecision;
			this.defficiency = defficiency;
			this.val = val;
			assert System.currentTimeMillis() - start <= 1 : "Constructor execution took more than 1ms: " + (System.currentTimeMillis() - start) + "ms";
		}
		
		public void fillTupleArrayWithValues(Object[] tupleArray) {
			for (int i = nextDecision; i < tupleArray.length; i++)
				tupleArray[i] = null;
			fillTupleArrayWithValuesRec(tupleArray);
		}
		
		private void fillTupleArrayWithValuesRec(Object[] tupleArray) {
			if (nextDecision <= 0)
				return;
			tupleArray[nextDecision - 1] = val;
			parent.fillTupleArrayWithValuesRec(tupleArray);
		}

		@Override
		public String toString() {
			return "Node [parent=" + parent + ", nextDecision=" + nextDecision + ", defficiency=" + defficiency + ", val=" + val + "]";
		}
	}

	private final List<? extends Collection<T>> sets;
	private final int numSets;
	private final Predicate<Object[]> prefixFilter;
	private int computedTuples = 0;
	private final Object[] currentTuple;
	private Queue<Node> open = new PriorityQueue<>((n1, n2) -> n1.defficiency - n2.defficiency);

	public LDSRelationComputer(List<? extends Collection<T>> sets) {
		this(new RelationComputationProblem<>(sets));
	}

	public LDSRelationComputer(RelationComputationProblem<T> problem) {
		super(problem);
		sets = problem.getSets();
		numSets = sets.size();
		prefixFilter = problem.getPrefixFilter();
		currentTuple = new Object[numSets];
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		switch (getState()) {
		case created: {
			open.add(new Node(null, 0, 0, null));
			return activate();
		}
		case active: {
			try {
				checkTermination();
			} catch (DelayedTimeoutCheckException | DelayedCancellationCheckException e) {
				e.printStackTrace();
			}
			long start = System.currentTimeMillis();
			long lastCheck = start;
			if (open.isEmpty())
				return terminate();

			/* determine next cheapest path to a leaf */
			Node next = null;
			while (!open.isEmpty() && (next = open.poll()).nextDecision < numSets) {
				int i = 0;
				for (T item : sets.get(next.nextDecision)) {
					long innerTimePoint = System.currentTimeMillis();
					next.fillTupleArrayWithValues(currentTuple);
					assert (System.currentTimeMillis() - innerTimePoint) < 5 : "Copying the " + (next.nextDecision) + "-tuple " + Arrays.toString(currentTuple) + " took " + (System.currentTimeMillis() - innerTimePoint) + "ms, which is way too much!";
					innerTimePoint = System.currentTimeMillis();
					boolean adopt = prefixFilter.test(currentTuple);
					assert (System.currentTimeMillis() - innerTimePoint) < 5 : "Testing the " + (next.nextDecision) + "-tuple " + Arrays.toString(currentTuple) + " took " + (System.currentTimeMillis() - innerTimePoint) + "ms, which is way too much!";
					if (adopt) {
						innerTimePoint = System.currentTimeMillis();
						int nextDecision = next.nextDecision + 1;
						int defficiency = next.defficiency + i++;
						assert (System.currentTimeMillis() - innerTimePoint) <= 5 : "Computing values for the next node for the " + (next.nextDecision) + "-tuple " + Arrays.toString(currentTuple) + " took " + (System.currentTimeMillis() - innerTimePoint) + "ms, which is way too much!";
						innerTimePoint = System.currentTimeMillis();
//						System.out.println(computedTuples);
						Node newNode = new Node();
						assert (System.currentTimeMillis() - innerTimePoint) < 750 : "Creating a new node took " + (System.currentTimeMillis() - innerTimePoint) + "ms, which is way too much! " + computedTuples + " tuples have been computed already.";
						newNode.parent = next;
						newNode.nextDecision = nextDecision;
						newNode.defficiency = defficiency;
						newNode.val = item;
						open.add(newNode);
					}
				}
			}

			/* at this point, next should contain a fully specified tuple */
			if (next == null || next.nextDecision < sets.size())
				return terminate();
			computedTuples++;
			return new TupleOfCartesianProductFoundEvent<>(Arrays.copyOf(currentTuple, sets.size()));
		}
		default:
			throw new IllegalStateException();
		}

	}

	@SuppressWarnings("unchecked")
	public Object[] nextTuple() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
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
	public List<Object[]> call() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		List<Object[]> product = new ArrayList<>();
		Object[] nextTuple;
		while ((nextTuple = nextTuple()) != null) {
			product.add(nextTuple);
		}
		return product;
	}

}
