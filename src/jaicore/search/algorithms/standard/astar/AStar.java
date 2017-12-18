package jaicore.search.algorithms.standard.astar;

import java.util.Iterator;
import java.util.List;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ParentDiscarding;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;

/**
 * A* algorithm implementation using the method design pattern.
 *
 * @author Felix Mohr
 */
public class AStar<T, A> extends BestFirst<T, A> {

	private static class FComputer<T> implements INodeEvaluator<T,Double> {

		private final AStarEdgeCost<T> g;
		private final INodeEvaluator<T,Double> h;

		public FComputer(AStarEdgeCost<T> g, INodeEvaluator<T,Double> h) {
			super();
			this.g = g;
			this.h = h;
		}

		@Override
		public Double f(Node<T,Double> node) throws Exception {
			List<Node<T,Double>> path = node.path();
			int depth = path.size() - 1;
			double pathCost = 0;
			double heuristic = h.f(node);
			if (depth > 0) {
				Iterator<Node<T,Double>> it = path.iterator();
				Node<T,Double> parent = it.next();
				Node<T,Double> current;
				while (it.hasNext()) {
					current = it.next();
					pathCost += g.g(parent, current);
					parent = current;
				}
			}
			return pathCost + heuristic;
		}
	}
	
	public AStar(GraphGenerator<T, A> graphGenerator, AStarEdgeCost<T> g, INodeEvaluator<T,Double> h) {
		super(graphGenerator, new FComputer<T>(g, h));
	}
	
	public AStar(GraphGenerator<T, A> graphGenerator, AStarEdgeCost<T> g, INodeEvaluator<T,Double> h,  ParentDiscarding pd) {
		super(graphGenerator, new FComputer<T>(g, h), pd);
	}

	
}