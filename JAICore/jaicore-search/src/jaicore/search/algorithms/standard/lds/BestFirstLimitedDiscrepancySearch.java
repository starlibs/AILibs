package jaicore.search.algorithms.standard.lds;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.graph.IGraphAlgorithmListener;
import jaicore.search.algorithms.standard.AbstractORGraphSearch;
import jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import jaicore.search.algorithms.standard.bestfirst.events.SuccessorComputationCompletedEvent;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.probleminputs.NodeRecommendedTree;
import jaicore.search.model.travesaltree.Node;

/**
 * This class conducts a limited discrepancy search by running a best first algorithm with list-based node evaluations
 * Since the f-values are lists too, we do not simply extend BestFirst but rather forward all commands to it
 * 
 * @author fmohr
 *
 * @param <T>
 * @param <A>
 * @param <V>
 */
public class BestFirstLimitedDiscrepancySearch<T, A, V extends Comparable<V>> extends AbstractORGraphSearch<NodeRecommendedTree<T, A>, EvaluatedSearchGraphPath<T, A, V>, T, A, V, Node<T, NodeOrderList>, A> {

	private final StandardBestFirst<T, A, NodeOrderList> bestFirst;

	private class OrderListNumberComputer implements INodeEvaluator<T, NodeOrderList>, IGraphAlgorithmListener<Node<T, NodeOrderList>, A> {
		private final Comparator<T> heuristic;
		private final Map<Node<T, ?>, List<T>> childOrdering = new HashMap<>();

		public OrderListNumberComputer(Comparator<T> heuristic) {
			super();
			this.heuristic = heuristic;
		}

		@Override
		public NodeOrderList f(Node<T, ?> node) throws Exception {
			NodeOrderList list = new NodeOrderList();
			Node<T, ?> parent = node.getParent();
			if (parent == null)
				return list;

			/* add the label sequence of the parent to this node*/
			list.addAll((NodeOrderList) parent.getInternalLabel());
			list.add(childOrdering.get(parent).indexOf(node.getPoint()));
			return list;
		}

		@Subscribe
		public void receiveSuccessorsCreatedEvent(SuccessorComputationCompletedEvent<T, ?> successorDescriptions) {
			List<T> successors = successorDescriptions.getSuccessorDescriptions().stream().map(n -> n.getTo()).sorted(heuristic).collect(Collectors.toList());
			childOrdering.put(successorDescriptions.getNode(), successors);
		}
	}
	
	public BestFirstLimitedDiscrepancySearch(NodeRecommendedTree<T, A> problem) {
		super(problem);
		OrderListNumberComputer nodeEvaluator = new OrderListNumberComputer(problem.getRecommender());
		this.bestFirst = new StandardBestFirst<>(new GeneralEvaluatedTraversalTree<>(problem.getGraphGenerator(), nodeEvaluator));
		bestFirst.registerListener(nodeEvaluator);
	}

	@Override
	public void cancel() {
		bestFirst.cancel();
	}

	@Override
	public void registerListener(Object listener) {
		bestFirst.registerListener(listener);
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		bestFirst.setNumCPUs(numberOfCPUs);
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		return bestFirst.nextWithException();
	}

	@Override
	public EvaluatedSearchGraphPath<T, A, V> call() throws Exception {
		EvaluatedSearchGraphPath<T, A, NodeOrderList> solution = bestFirst.call();
		EvaluatedSearchGraphPath<T, A, V> modifiedSolution = new EvaluatedSearchGraphPath<T,A,V>(solution.getNodes(), solution.getEdges(), null);
		return modifiedSolution;
	}

	@Override
	public EvaluatedSearchGraphPath<T, A, V> getSolutionProvidedToCall() {
		return null;
	}

	@Override
	public int getNumCPUs() {
		return 0;
	}
}
