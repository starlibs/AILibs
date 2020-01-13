package ai.libs.jaicore.search.probleminputs;

import java.util.List;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.common.math.IMetric;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<N, A> extends GraphSearchWithNumberBasedAdditivePathEvaluation<N, A> {

	public interface PathCostEstimator<N, A> {
		public double h(BackPointerPath<N, A, ?> from, BackPointerPath<N, A, ?> to);
	}

	public interface DistantSuccessorGenerator<N> {
		public List<N> getDistantSuccessors(N node, int k, IMetric<N> metricOverStates, double delta) throws InterruptedException;
	}

	public static class SubPathEvaluationBasedFComputer<N, A> extends GraphSearchWithNumberBasedAdditivePathEvaluation.FComputer<N, A> {

		private final PathCostEstimator<N, A> hPath; // this is to estimate the minimum cost between to concretely defined nodes

		public SubPathEvaluationBasedFComputer(final EdgeCostComputer<N, A> g, final IPathEvaluator<N, A, Double> h, final PathCostEstimator<N, A> hPath) {
			super(g, h);
			this.hPath = hPath;
		}

		public double h(final BackPointerPath<N, A, ?> from, final BackPointerPath<N, A, ?> to) {
			return this.hPath.h(from, to);
		}

		public PathCostEstimator<N, A> gethPath() {
			return this.hPath;
		}
	}

	private final IMetric<N> metricOverStates;
	private final DistantSuccessorGenerator<N> distantSuccessorGenerator;

	public GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic(final IPathSearchInput<N, A> graphSearchInput, final EdgeCostComputer<N, A> g, final IPathEvaluator<N, A, Double> h, final PathCostEstimator<N, A> hPath,
			final IMetric<N> metricOverStates, final DistantSuccessorGenerator<N> distantSuccessorGenerator) {
		super(graphSearchInput, new SubPathEvaluationBasedFComputer<>(g, h, hPath));
		this.metricOverStates = metricOverStates;
		this.distantSuccessorGenerator = distantSuccessorGenerator;
	}

	public GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic(final IGraphGenerator<N, A> graphGenerator, final IPathGoalTester<N, A> goalTester, final EdgeCostComputer<N, A> g, final IPathEvaluator<N, A, Double> h, final PathCostEstimator<N, A> hPath,
			final IMetric<N> metricOverStates, final DistantSuccessorGenerator<N> distantSuccessorGenerator) {
		super(graphGenerator, goalTester, new SubPathEvaluationBasedFComputer<>(g, h, hPath));
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
