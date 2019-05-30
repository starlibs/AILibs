package jaicore.search.probleminputs;

import java.util.Iterator;
import java.util.List;

import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.Node;

public class GraphSearchWithNumberBasedAdditivePathEvaluation<N, A> extends GraphSearchWithSubpathEvaluationsInput<N, A, Double> {

	public interface EdgeCostComputer<N> {
		public double g(Node<N, ?> from, Node<N, ?> to);
	}

	public static class FComputer<N> implements INodeEvaluator<N, Double> {

		private final EdgeCostComputer<N> g;
		private final INodeEvaluator<N, Double> h; // this is to estimate the minimum cost between a node and ANY goal node

		public FComputer(final EdgeCostComputer<N> g, final INodeEvaluator<N, Double> h) {
			super();
			this.g = g;
			this.h = h;
		}

		@Override
		public Double f(final Node<N, ?> node) throws NodeEvaluationException, InterruptedException {
			List<?> path = node.path();
			int depth = path.size() - 1;
			double pathCost = 0;
			double heuristic = this.h.f(node);
			if (depth > 0) {
				@SuppressWarnings("unchecked")
				Iterator<Node<N, ?>> it = (Iterator<Node<N, ?>>) path.iterator();
				Node<N, ?> parent = it.next();
				Node<N, ?> current;
				while (it.hasNext()) {
					current = it.next();
					pathCost += this.g.g(parent, current);
					parent = current;
				}
			}
			return pathCost + heuristic;
		}

		public EdgeCostComputer<N> getG() {
			return this.g;
		}

		public INodeEvaluator<N, Double> getH() {
			return this.h;
		}
	}

	public GraphSearchWithNumberBasedAdditivePathEvaluation(final GraphGenerator<N, A> graphGenerator, final EdgeCostComputer<N> g, final INodeEvaluator<N, Double> h) {
		super(graphGenerator, new FComputer<>(g, h));
	}

	/**
	 * This constructor can be used if one wants to extend AStar by some more specific f-value computer.
	 * See R* for an example.
	 *
	 * @param graphGenerator
	 * @param fComputer
	 */
	public GraphSearchWithNumberBasedAdditivePathEvaluation(final GraphGenerator<N, A> graphGenerator, final FComputer<N> fComputer) {
		super(graphGenerator, fComputer);
	}

}
