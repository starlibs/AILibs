package jaicore.search.model.probleminputs;

import java.util.Iterator;
import java.util.List;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.Node;

public class NumberBasedAdditiveTraversalTree<N,A> extends GeneralEvaluatedTraversalTree<N, A, Double> {

	public interface EdgeCostComputer<N> {
		public double g(Node<N,?> from, Node<N,?> to);
	}
	
	private static class FComputer<N> implements INodeEvaluator<N,Double> {

		private final EdgeCostComputer<N> g;
		private final INodeEvaluator<N,Double> h;

		public FComputer(EdgeCostComputer<N> g, INodeEvaluator<N,Double> h) {
			super();
			this.g = g;
			this.h = h;
		}

		@Override
		public Double f(Node<N,?> node) throws Exception {
			List<?> path = node.path();
			int depth = path.size() - 1;
			double pathCost = 0;
			double heuristic = h.f(node);
			if (depth > 0) {
				@SuppressWarnings("unchecked")
				Iterator<Node<N,?>> it = (Iterator<Node<N, ?>>) path.iterator();
				Node<N,?> parent = it.next();
				Node<N,?> current;
				while (it.hasNext()) {
					current = it.next();
					pathCost += g.g(parent, current);
					parent = current;
				}
			}
			return pathCost + heuristic;
		}
	}
	
	public NumberBasedAdditiveTraversalTree(GraphGenerator<N, A> graphGenerator, EdgeCostComputer<N> g, INodeEvaluator<N, Double> h) {
		super(graphGenerator, new FComputer<>(g, h));
	}

}
