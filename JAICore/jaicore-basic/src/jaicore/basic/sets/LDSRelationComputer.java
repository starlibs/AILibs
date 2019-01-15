package jaicore.basic.sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

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
public class LDSRelationComputer<T> extends AAlgorithm<RelationComputationProblem<T>, List<List<T>>> {

	private class Node {
		Node parent;
		int defficiency;
		int indexOfSet;
		int indexOfValue;

		public Node() {
			
		}
		public Node(Node parent, int indexOfSet, int defficiency, int indexInSet) {
			long start = System.currentTimeMillis();
			this.parent = parent;
			this.indexOfSet = indexOfSet;
			this.defficiency = defficiency;
			this.indexOfValue = indexInSet;
			assert System.currentTimeMillis() - start <= 1 : "Constructor execution took more than 1ms: " + (System.currentTimeMillis() - start) + "ms";
		}
		
		public void fillTupleArrayWithValues(List<T> tupleArray) {
			tupleArray.clear();
			fillTupleArrayWithValuesRec(tupleArray);
		}
		
		private void fillTupleArrayWithValuesRec(List<T> tupleArray) {
			if (parent == null)
				return;
			tupleArray.add(0, getObject());
			parent.fillTupleArrayWithValuesRec(tupleArray);
		}

		@Override
		public String toString() {
			return "Node [parent=" + parent + ", indexOfSet=" + indexOfSet + ", defficiency=" + defficiency + ", indexInSet=" + indexOfValue + "]";
		}
		
		T getObject() {
			return sets.get(indexOfSet).get(indexOfValue);
		}
		
		/**
		 * this method can be used to store old nodes in order to use them again later in the algorithm.
		 * This can be useful to avoid the creation of new objects all the time, which can be time consuming.
		 */
		public void recycle() {
			if (indexOfSet >= numSets) {
				recycledNodes.add(this);
			}
//			System.out.println(indexOfValue +"/" + sets.get(parent.nextDecision).size());
//			if (parent != null && indexOfValue == sets.get(parent.nextDecision).size()) {
//				System.out.println("Recycling");
//				parent.recycle();
//			}
		}
	}

	private final List<List<T>> sets;
	private final int numSets;
	private final Predicate<List<T>> prefixFilter;
	private int computedTuples = 0;
	private final List<T> currentTuple;
	private Queue<Node> open = new PriorityQueue<>((n1, n2) -> n1.defficiency - n2.defficiency);
	private List<Node> recycledNodes = new ArrayList<>();
	private int numRecycledNodes;
	private int numCreatedNodes;

	public LDSRelationComputer(List<? extends Collection<T>> sets) {
		this(new RelationComputationProblem<>(sets));
	}

	public LDSRelationComputer(RelationComputationProblem<T> problem) {
		super(problem);
		sets = new ArrayList<>();
		for (Collection<T> set : problem.getSets())
			sets.add(set instanceof List ? (List<T>)set : new ArrayList<>(set));
		numSets = sets.size();
		prefixFilter = problem.getPrefixFilter();
		currentTuple = new ArrayList<>();
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		switch (getState()) {
		case created: {
			open.add(new Node(null, -1, 0, 0));
			numCreatedNodes++;
			return activate();
		}
		case active: {
			try {
				checkTermination();
			} catch (DelayedTimeoutCheckException | DelayedCancellationCheckException e) {
				e.printStackTrace();
			}
			if (open.isEmpty())
				return terminate();

			/* determine next cheapest path to a leaf */
			Node next = null;
			Node newNode;
			while (!open.isEmpty() && (next = open.poll()).indexOfSet < numSets - 1) {
				int i = 0;
				int setIndex = next.indexOfSet + 1;
				List<T> set = sets.get(setIndex);
				int n = set.size();
				next.fillTupleArrayWithValues(currentTuple); // get current tuple
				for (int j = 0; j < n; j++) {
					try {
						checkTermination();
					} catch (DelayedTimeoutCheckException | DelayedCancellationCheckException e) {
						e.printStackTrace();
					}
					long innerTimePoint = System.currentTimeMillis();
					currentTuple.add(set.get(j));
					assert (System.currentTimeMillis() - innerTimePoint) < 5 : "Copying the " + (next.indexOfSet) + "-tuple " + currentTuple + " took " + (System.currentTimeMillis() - innerTimePoint) + "ms, which is way too much!";
					innerTimePoint = System.currentTimeMillis();
					boolean adopt = prefixFilter.test(currentTuple);
					assert (System.currentTimeMillis() - innerTimePoint) < 1000 : "Testing the " + (next.indexOfSet) + "-tuple " + currentTuple + " took " + (System.currentTimeMillis() - innerTimePoint) + "ms, which is way too much!";
					if (adopt) {
						innerTimePoint = System.currentTimeMillis();
						if (recycledNodes.isEmpty()) {
							newNode = new Node();
							numCreatedNodes++;
							assert (System.currentTimeMillis() - innerTimePoint) < 1000 : "Creating a new node took " + (System.currentTimeMillis() - innerTimePoint) + "ms, which is way too much!\n" + computedTuples + " tuples have been computed already.\nRecycling list contains " + recycledNodes.size() + "\nOPEN contains " + open.size();
						}
						else {
							newNode = recycledNodes.remove(0);
							assert (System.currentTimeMillis() - innerTimePoint) < 10 : "Retrieving node from recycle list " + (System.currentTimeMillis() - innerTimePoint) + "ms, which is way too much!\n" + computedTuples + " tuples have been computed already.\nRecycling list contains " + recycledNodes.size() + "\nOPEN contains " + open.size();
						}
						newNode.parent = next;
						newNode.indexOfSet = next.indexOfSet + 1;
						newNode.defficiency = next.defficiency + i++;
						newNode.indexOfValue = j;
						open.add(newNode);
					}
					currentTuple.remove(setIndex);
				}
			}

			/* at this point, next should contain a fully specified tuple. If no next element exists, or the chosen node is not a leaf, terminate */
			if (next == null || next.indexOfSet < sets.size() - 1) {
				return terminate();
			}
			computedTuples++;
			
			/* load tuple of selected leaf node */
			next.fillTupleArrayWithValues(currentTuple);
			
			/* recycle leaf node and possibly also inner nodes*/
			next.recycle();
			numRecycledNodes++;
			List<T> tuple = new ArrayList<>(currentTuple);
			assert currentTuple.size() == numSets : "Tuple " + currentTuple + " should contain " + numSets + " elements but has " + currentTuple.size();
			return new TupleOfCartesianProductFoundEvent<>(tuple);
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

	public int getNumRecycledNodes() {
		return numRecycledNodes;
	}

	public int getNumCreatedNodes() {
		return numCreatedNodes;
	}
}
