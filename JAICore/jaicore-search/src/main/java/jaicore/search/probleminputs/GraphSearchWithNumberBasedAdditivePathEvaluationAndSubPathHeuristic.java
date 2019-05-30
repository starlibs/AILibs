package jaicore.search.probleminputs;

import java.util.List;

import jaicore.basic.IMetric;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.Node;

public class GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<N, A> extends GraphSearchWithNumberBasedAdditivePathEvaluation<N, A> {

	public interface PathCostEstimator<N> {
		public double h(Node<N, ?> from, Node<N, ?> to);
	}

	public interface DistantSuccessorGenerator<N> {
		public List<N> getDistantSuccessors(N node, int k, IMetric<N> metricOverStates, double delta) throws InterruptedException;
	}

	public static class SubPathEvaluationBasedFComputer<N> extends GraphSearchWithNumberBasedAdditivePathEvaluation.FComputer<N> {


		private final PathCostEstimator<N> hPath; // this is to estimate the minimum cost between to concretely defined nodes

		public SubPathEvaluationBasedFComputer(final EdgeCostComputer<N> g, final INodeEvaluator<N, Double> h, final PathCostEstimator<N> hPath) {
			super(g, h);
			this.hPath = hPath;
		}

		public double h(final Node<N, ?> from, final Node<N, ?> to) {
			return this.hPath.h(from, to);
		}

		public PathCostEstimator<N> gethPath() {
			return this.hPath;
		}
	}

	private final IMetric<N> metricOverStates;
	private final DistantSuccessorGenerator<N> distantSuccessorGenerator;

	public GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic(final GraphGenerator<N, A> graphGenerator, final EdgeCostComputer<N> g, final INodeEvaluator<N, Double> h, final PathCostEstimator<N> hPath, final IMetric<N> metricOverStates, final DistantSuccessorGenerator<N> distantSuccessorGenerator) {
		super(graphGenerator, new SubPathEvaluationBasedFComputer<>(g, h, hPath));
		this.metricOverStates = metricOverStates;
		this.distantSuccessorGenerator = distantSuccessorGenerator;
	}

	public IMetric<N> getMetricOverStates() {
		return this.metricOverStates;
	}

	public DistantSuccessorGenerator<N> getDistantSuccessorGenerator() {
		return this.distantSuccessorGenerator;
	}
}
