package jaicore.search.algorithms.standard.astar;

import java.util.Iterator;
import java.util.List;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;

/**
 * A* algorithm implementation using the method design pattern.
 *
 * @author Felix Mohr
 */
public class AStar<T, A> extends BestFirst<T, A> {

	private static class FComputer<T> implements INodeEvaluator<T,Integer> {

		private final AStarEdgeCost<T> g;
		private final INodeEvaluator<T,Integer> h;

		public FComputer(AStarEdgeCost<T> g, INodeEvaluator<T,Integer> h) {
			super();
			this.g = g;
			this.h = h;
		}

		@Override
		public Integer f(Node<T,Integer> node) throws Exception {
			List<Node<T,Integer>> path = node.path();
			int depth = path.size() - 1;
			int pathCost = 0;
			int heuristic = h.f(node);
			if (depth > 0) {
				Iterator<Node<T,Integer>> it = path.iterator();
				Node<T,Integer> parent = it.next();
				Node<T,Integer> current;
				while (it.hasNext()) {
					current = it.next();
					pathCost += g.g(parent, current);
					parent = current;
				}
			}
			return pathCost + heuristic;
		}
	}
	
	public AStar(GraphGenerator<T, A> graphGenerator, AStarEdgeCost<T> g, INodeEvaluator<T,Integer> h) {
		super(graphGenerator, new FComputer<T>(g, h));
	}

	
}