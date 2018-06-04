package jaicore.search.standardproblems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import jaicore.basic.IObjectEvaluator;
import jaicore.order.SetUtil.Pair;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class BiPartitionFinder<T> {

	/**
	 * Class to maintain the choices
	 * 
	 * @author fmohr
	 *
	 */
	private class BFNode {
		Collection<T> itemsOnLeft;
		Collection<T> itemsOnRight;

		public BFNode(Collection<T> itemsOnLeft, Collection<T> itemsOnRight) {
			super();
			this.itemsOnLeft = itemsOnLeft;
			this.itemsOnRight = itemsOnRight;
		}

		@Override
		public String toString() {
			return "BFNode [itemsOnLeft=" + itemsOnLeft + ", itemsOnRight=" + itemsOnRight + "]";
		}
	}
	
	private class BFGraphGenerator implements GraphGenerator<BFNode, Boolean> {
		
		@Override
		public SingleRootGenerator<BFNode> getRootGenerator() {
			return () -> new BFNode(new HashSet<>(), new HashSet<>());
		}

		@Override
		public SuccessorGenerator<BiPartitionFinder<T>.BFNode, Boolean> getSuccessorGenerator() {
			return n -> {
				Collection<NodeExpansionDescription<BFNode,Boolean>> successors = new ArrayList<>();
				int k = n.itemsOnLeft.size() + n.itemsOnRight.size();
				T newItem = items.get(k);
				Collection<T> aLeft = new HashSet<>(n.itemsOnLeft);
				aLeft.add(newItem);
				Collection<T> aRight = new HashSet<>(n.itemsOnRight);
				aRight.add(newItem);
				successors.add(new NodeExpansionDescription<BiPartitionFinder<T>.BFNode, Boolean>(n, new BFNode(aLeft, n.itemsOnRight), true, NodeType.OR));
				successors.add(new NodeExpansionDescription<BiPartitionFinder<T>.BFNode, Boolean>(n, new BFNode(n.itemsOnLeft, aRight), false, NodeType.OR));
				return successors;
			};
		}

		@Override
		public NodeGoalTester<BiPartitionFinder<T>.BFNode> getGoalTester() {
			return n -> n.itemsOnLeft.size() + n.itemsOnRight.size() == items.size();
		}

		@Override
		public boolean isSelfContained() {
			return true;
		}

		@Override
		public void setNodeNumbering(boolean nodenumbering) {
			throw new UnsupportedOperationException("No node numbering supported.");
		}
	}
	
	private final List<T> items;
	private final BestFirst<BFNode, Boolean> search;

	public BiPartitionFinder(Collection<T> items, IObjectEvaluator<Pair<Collection<T>,Collection<T>>, Double> evaluator) {
		super();
		this.items = new ArrayList<>(items);
		this.search = new BestFirst<BFNode,Boolean>(new BFGraphGenerator(), n -> evaluator.evaluate(new Pair<>(n.getPoint().itemsOnLeft, n.getPoint().itemsOnRight)));
	}
	
	public Pair<Collection<T>,Collection<T>> getPartition() {
		new SimpleGraphVisualizationWindow<>(this.search);
		List<BFNode> solution = this.search.nextSolution();
		BFNode lastNode = solution.get(solution.size() - 1);
		return new Pair<>(lastNode.itemsOnLeft, lastNode.itemsOnRight);
	}
}
