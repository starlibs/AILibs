package jaicore.search.algorithms.standard.generalbestfirst;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import jaicore.graph.Graph;
import jaicore.search.algorithms.standard.core.ANDORGraphSearch;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 * A* algorithm implementation using the method design pattern.
 *
 * @author Felix Mohr
 */
public class RandomizedAndOrSearch<T,A> extends ANDORGraphSearch<T,A,Integer> {


	private Node<T,Integer> root;
	private final Queue<Node<T,Integer>> open = new LinkedList<>();
	
	public RandomizedAndOrSearch(RootGenerator<T> rootGenerator, SuccessorGenerator<T, A> successorGenerator, GoalTester<T> goalTester) {
		super(rootGenerator, successorGenerator, goalTester);
	}

	@Override
	protected Node<T,Integer> initialize() {
		root = getOrNode(null, rootGenerator.getRoots().iterator().next(), null);
		open.add(root);
		return root;
	}

	@Override
	protected Graph<Node<T,Integer>> nextSolutionBase() {
		return null;
	}
	
	@Override
	protected Node<T,Integer> nextNode(Graph<Node<T,Integer>> solutionBase) {
		return open.poll();
	}

	@Override
	protected Collection<Node<T,Integer>> expand(Node<T,Integer> expanded) {
		List<NodeExpansionDescription<T, A>> successorNodes = successorGenerator.generateSuccessors(expanded);
		Collection<Node<T,Integer>> successors = new ArrayList<>();
		for (NodeExpansionDescription<T, A> successorDescription : successorNodes) {
			
			/* no reopening of nodes we already know */
			T successor = successorDescription.getTo();
			boolean isKnown = ext2int.containsKey(successor);
			Node<T,Integer> node = null;
			NodeType type = successorDescription.getTypeOfToNode();
			if (type == NodeType.AND)
				node = getAndNode(expanded, successor, successorDescription.getAction());
			if (type == NodeType.OR)
				node = getOrNode(expanded, successor, successorDescription.getAction());
			successors.add(node);
			if (!isKnown && !goalTester.isGoal(node)) {
				open.add(node);
			}
		}
		return successors;
	}
	
	@Override
	protected int getNumberOfOpenNodesInSolutionBase(Graph<Node<T,Integer>> solutionBase) {
		return open.size();
	}
}