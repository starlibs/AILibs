package jaicore.search.algorithms.standard.lds;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.eventbus.Subscribe;

import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.algorithms.standard.core.events.SuccessorComputationCompletedEvent;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;


public class BestFirstLimitedDiscrepancySearch<T,A> extends ORGraphSearch<T, A, NodeOrderList>{
	
	private static class OrderListNumberComputer<T> implements INodeEvaluator<T,NodeOrderList> {

		private final Comparator<T> heuristic;
		private final Map<Node<T,?>, List<T>> childOrdering = new HashMap<>();
		
		public OrderListNumberComputer(Comparator<T> heuristic) {
			super();
			this.heuristic = heuristic;
		}

		@Override
		public NodeOrderList f(Node<T, ?> node) throws Throwable {
			NodeOrderList list = new NodeOrderList();
			Node<T,?> parent = node.getParent();
			if (parent == null)
				return list;
			
			/* add the label sequence of the parent to this node*/
			list.addAll((NodeOrderList)parent.getInternalLabel());
			list.add(childOrdering.get(parent).indexOf(node.getPoint()));
			return list;
		}
		
		@Subscribe
		public void receiveSuccessorsCreatedEvent(SuccessorComputationCompletedEvent<T, ?> successorDescriptions) {
			List<T> successors = successorDescriptions.getSuccessorDescriptions().stream().map(n -> n.getTo()).sorted(heuristic).collect(Collectors.toList());
			childOrdering.put(successorDescriptions.getNode(), successors);
		}
	}

	public BestFirstLimitedDiscrepancySearch(GraphGenerator<T, A> graphGenerator, Comparator<T> heuristic) {
		super(graphGenerator, new OrderListNumberComputer<>(heuristic));
		this.registerListener(getNodeEvaluator());
	}

}
